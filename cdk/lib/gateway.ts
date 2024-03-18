import {Construct} from "constructs";
import type {GuStack} from "@guardian/cdk/lib/constructs/core";
import {CorsHttpMethod, HttpApi, VpcLink} from "aws-cdk-lib/aws-apigatewayv2"
import {HttpAlbIntegration} from "aws-cdk-lib/aws-apigatewayv2-integrations";
import {IApplicationLoadBalancer} from "aws-cdk-lib/aws-elasticloadbalancingv2";
import {ISecurityGroup, IVpc, Peer, Port, SubnetSelection} from "aws-cdk-lib/aws-ec2";
import {GuSecurityGroup} from "@guardian/cdk/lib/constructs/ec2";
import {IApplicationListener} from "aws-cdk-lib/aws-elasticloadbalancingv2/lib/alb/application-listener";
import {Duration} from "aws-cdk-lib";
import {hostingDomain} from "./constants";

export type ValidStages = "CODE-AARDVARK"|"PROD-AARDVARK"|"CODE-ZEBRA"|"PROD-ZEBRA";

interface HttpGatewayProps {
    stage: ValidStages;
    previewMode: boolean;
    backendLoadbalancer: IApplicationLoadBalancer;
    backendListener: IApplicationListener;
    backendLbIncomingSg: ISecurityGroup;
    lbDomainName: string;
    subnets: SubnetSelection;
    vpc: IVpc;
}
export class HttpGateway extends Construct {
    constructor(scope: GuStack, id: string, props: HttpGatewayProps) {
        super(scope, id);

        const sg = new GuSecurityGroup(scope, "VpcLinkSG", {
            app: "concierge-graphql",
            vpc: props.vpc,
            allowAllOutbound: false,
            egresses: [
                {
                    range: Peer.securityGroupId(props.backendLbIncomingSg.securityGroupId),
                    port: Port.tcp(443),
                    description: "Access to incoming security group of the backend loadbalancer"
                }
            ]
        });

        const vpcLink = new VpcLink(this, "ApiGWVPC", {
            securityGroups: [sg],
            subnets: props.subnets,
            vpc: props.vpc,
            vpcLinkName: `VpcLink-concierge-graphql-${props.stage}`
        });

        const maybePreview = props.previewMode ? "preview-" : "";
        const deployedUrl = hostingDomain[props.stage];

        const httpApi = new HttpApi(this, "ApiGW", {
            apiName: `concierge-graphql-${maybePreview}${props.stage}`,
            description: `Gateway for the ${props.stage} concierge-graphql${maybePreview} instance`,
            defaultIntegration: new HttpAlbIntegration('DefaultIntegration', props.backendListener, {
                vpcLink,
                secureServerName: props.lbDomainName,
            }),
            corsPreflight: {
                allowOrigins: ['http://localhost:8081', `https://${deployedUrl}`],
                allowMethods: [CorsHttpMethod.POST, CorsHttpMethod.GET, CorsHttpMethod.OPTIONS],
                allowHeaders: ['content-type', 'x-api-key'],
                maxAge: Duration.minutes(5),
                allowCredentials: true
            },
            createDefaultStage: true,
        });
        //
        // const plan = new CfnUsagePlan(this, "GQLUsagePlan", {
        //     apiStages: [
        //         {
        //             apiId: httpApi.apiId,
        //             stage: httpApi.defaultStage?.stageName,
        //             throttle: {
        //                 "$default": {
        //                     burstLimit: 150,
        //                     rateLimit: 50
        //                 }
        //             }
        //         }
        //     ],
        //     description: "Usage plan for access to concierge-graphql"
        // });
        // plan.node.addDependency(httpApi);

    }
}