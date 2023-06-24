import type {GuStackProps} from "@guardian/cdk/lib/constructs/core";
import {GuParameter, GuStack} from "@guardian/cdk/lib/constructs/core";
import type {App} from "aws-cdk-lib";
import {GuPlayApp} from "@guardian/cdk";
import {InstanceClass, InstanceSize, InstanceType, Peer, Port, Subnet, Vpc} from "aws-cdk-lib/aws-ec2";
import {AccessScope} from "@guardian/cdk/lib/constants";
import {aws_ssm} from "aws-cdk-lib";
import {getHostName} from "./hostname";
import {GuSecurityGroup} from "@guardian/cdk/lib/constructs/ec2";

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

    const app = new GuPlayApp(this, {
      access: {
        //You should put Kong gateway in front of this
        scope: AccessScope.INTERNAL,
        cidrRanges: [Peer.ipv4("10.0.0.0/8")],
      },
      app: "concierge-graphql",
      certificateProps: {
        hostedZoneId,
        domainName: getHostName(this),
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
      userData: {
        distributable: {
          fileName: "concierge-graphql_0.1.0_all.deb",
          executionStatement: "dpkg -i concierge-graphql_0.1.0_all.deb"
        }
      },
      vpc,
    });

    //Note - this SG will need to be manually added to the incoming rules of the appropriate ES instance to allow contact
    app.autoScalingGroup.addSecurityGroup(new GuSecurityGroup(this, "ESAccess", {
      app: "concierge-graphql",
      description: "Allow access to Elasticsearch",
      vpc,
      egresses: [
        {
          range: Peer.ipv4("10.0.0.0/24"),
          port: Port.tcp(9200),
          description: "Allow outgoing to Elasticsearch data port"
        }
      ]
    }))
  }

  getAccountPath(scope:GuStack, isPreview:boolean, elementName: string) {
    const basePath = "/account/vpc";
    if(isPreview) {
      return scope.stage=="CODE" ? `${basePath}/CODE-preview/${elementName}` : `${basePath}/PROD-preview/${elementName}`;
    } else {
      return scope.stage=="CODE" ? `${basePath}/CODE-live/${elementName}` : `${basePath}/PROD-live/${elementName}`;
    }
  }

  getVpcIdPath(scope:GuStack, isPreview:boolean) {
    return this.getAccountPath(scope, isPreview,"id");
  }

  getDeploymentSubnetsPath(scope:GuStack, isPreview:boolean) {
    return this.getAccountPath(scope, isPreview,"subnets")
  }
}
