import type {GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {GuParameter, GuStack} from "@guardian/cdk/lib/constructs/core";
import type {App} from "aws-cdk-lib";
import {aws_ssm} from "aws-cdk-lib";
import {GuPlayApp} from "@guardian/cdk";
import {InstanceClass, InstanceSize, InstanceType, Peer, Port, Subnet, Vpc} from "aws-cdk-lib/aws-ec2";
import {AccessScope} from "@guardian/cdk/lib/constants";
import {getHostName} from "./hostname";
import {GuSecurityGroup, GuVpc} from "@guardian/cdk/lib/constructs/ec2";
import {HttpGateway, ValidStages} from "./gateway";
import {AttributeType, BillingMode, Table} from "aws-cdk-lib/aws-dynamodb";
import {GuPolicy} from "@guardian/cdk/lib/constructs/iam";
import {Effect, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {StringParameter} from "aws-cdk-lib/aws-ssm";
import {GraphiqlExplorer} from "./graphiql-explorer";

export class ConciergeGraphql extends GuStack {
  constructor(scope: App, id: string, props: GuStackProps) {
    super(scope, id, props);

    const previewMode = this.stack.endsWith("-preview");

    //Preview needs to live in a VPC so we can route to capi-preview
    const vpcId = new GuParameter(this, "vpcId", {
      description: "VPC to deploy into",
      default: this.getVpcIdPath(this, previewMode),
      fromSSM: true,
      type: "String"
    });

    const subnetsList = new GuParameter(this, "subnets", {
      description: "Subnets to deploy into",
      default: this.getDeploymentSubnetsPath(this, previewMode),
      fromSSM: true,
      type: "List<String>"
    });

    const vpc = Vpc.fromVpcAttributes(this, "vpc", {
      vpcId: vpcId.valueAsString,
      //len(publicSubnetIds) must be a multiple of len(availabilityZones)
      availabilityZones: ["eu-west-1a","eu-west-1b" ,"eu-west-1c"].slice(0, subnetsList.valueAsList.length),
      publicSubnetIds: subnetsList.valueAsList,
    });

    const allSubnets = subnetsList.valueAsList.map((id, ctr)=>Subnet.fromSubnetId(this, `Subnet${ctr}`, id));

    const hostedZoneId = aws_ssm.StringParameter.valueForStringParameter(this, `/account/services/capi.gutools/${this.stage}/hostedzoneid`);

    const lbDomainName = getHostName(this, ".internal");

    const authTable = new Table(this, "AuthTable", {
      billingMode: BillingMode.PAY_PER_REQUEST,
      partitionKey: {
        name: "ApiKey",
        type: AttributeType.STRING
      }
    });

    new StringParameter(this, "AuthTableParam", {
      parameterName: `/${this.stage}/${this.stack}/concierge-graphql/aws/auth_table`,
      stringValue: authTable.tableName
    });

    const {loadBalancer, listener, autoScalingGroup} = new GuPlayApp(this, {
      access: {
        //You should put a gateway in front of this
        scope: AccessScope.INTERNAL,
        cidrRanges: [Peer.ipv4("10.0.0.0/8")],
      },
      app: "concierge-graphql",
      certificateProps: {
        hostedZoneId,
        domainName: lbDomainName,
      },
      instanceType: InstanceType.of(InstanceClass.T4G, InstanceSize.LARGE),
      monitoringConfiguration: {
        noMonitoring: true,
      },
      privateSubnets: allSubnets,
      publicSubnets: allSubnets,
      scaling: {
        minimumInstances: 2,
        maximumInstances: 4,
      },
      applicationLogging: {
        enabled: true,
      },
      userData: {
        distributable: {
          fileName: "concierge-graphql_0.1.0_all.deb",
          executionStatement: "dpkg -i concierge-graphql/concierge-graphql_0.1.0_all.deb"
        }
      },
      roleConfiguration: {
        additionalPolicies: [
            new GuPolicy(this, "AuthTablePolicy", {
              statements: [ new PolicyStatement({
                effect: Effect.ALLOW,
                actions: ["dynamodb:GetItem"],
                resources: [authTable.tableArn]
              })]
            })
        ]
      },
      vpc,
    });

    const linkageSG = new GuSecurityGroup(this, "LinkageSG", {
      app: props.app ?? "concierge-graphql",
      vpc,
    });
    loadBalancer.addSecurityGroup(linkageSG);

    const subnets = GuVpc.subnets(this, subnetsList.valueAsList);

    new HttpGateway(this, "GW", {
      stage: props.stage as ValidStages,
      backendLoadbalancer: loadBalancer,
      lbDomainName,
      previewMode,
      backendListener: listener,
      backendLbIncomingSg: linkageSG,
      subnets: {
        subnets,
      },
      vpc
    });

    autoScalingGroup.connections.allowTo(Peer.ipv4("10.0.0.0/8"), Port.tcp(9200), "Allow outgoing connections to Elasticsearch");

    new GraphiqlExplorer(this, "Explorer", {
      appName: "graphiql-explorer"  //needs to match the value in riff-raff.yaml
    })
    //OK - so this is a good idea and should really be in here. But it's damn fiddly so leaving it out for now.
    //The idea is we need a connection to the relevant Elasticsearch instance. So, we define a "connection" (which basically
    //to an egress rule) on our SG which allows egress to the remote ES SG. You still manually need to add a rule on the relevant
    //remove Elasticsearch SG to allow ingress from us.
    //Because there is still a manual stage, I'm going to leave it as manual for now, and leave the code here for reference.

    // const elasticsearchSGID = new GuParameter(this, "ESConnectionID", {
    //   description: "Security group ID for the elasticsearch cluster to connect to",
    //   default: `/${this.stage}/${this.stack}/elasticsearch/securityGroupId`,
    //   fromSSM: true,
    //   type: "String"
    // });
    // const elasticsearchSG = GuSecurityGroup.fromSecurityGroupId(this, "ESConnectionSG", elasticsearchSGID.valueAsString);
    //
    // app.autoScalingGroup.connections.allowTo(elasticsearchSG, Port.tcp(9200), "Allow connection to Elasticsearch")
    // //Note - this SG will need to be manually added to the incoming rules of the appropriate ES instance to allow contact
    // app.autoScalingGroup.connections.addSecurityGroup(new GuSecurityGroup(this, "ESAccess", {
    //   app: "concierge-graphql",
    //   description: "Allow access to Elasticsearch",
    //   vpc,
    //   allowAllOutbound: false,
    //   egresses: [
    //     {
    //       range: Peer.ipv4("10.0.0.0/24"),
    //       port: Port.tcp(9200),
    //       description: "Allow outgoing to Elasticsearch data port"
    //     }
    //   ]
    // }))
  }

  getAccountPath(scope:GuStack, isPreview:boolean, elementName: string) {
    const basePath = "/account/vpc";
    if(isPreview) {
      return scope.stage.startsWith("CODE") ? `${basePath}/CODE-preview/${elementName}` : `${basePath}/PROD-preview/${elementName}`;
    } else {
      return scope.stage.startsWith("CODE") ? `${basePath}/CODE-live/${elementName}` : `${basePath}/PROD-live/${elementName}`;
    }
  }

  getVpcIdPath(scope:GuStack, isPreview:boolean) {
    return this.getAccountPath(scope, isPreview,"id");
  }

  getDeploymentSubnetsPath(scope:GuStack, isPreview:boolean) {
    return this.getAccountPath(scope, isPreview,"subnets")
  }

}
