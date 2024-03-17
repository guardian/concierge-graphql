import {Construct} from "constructs";
import type { GuStack } from "@guardian/cdk/lib/constructs/core";
import { GuStringParameter } from "@guardian/cdk/lib/constructs/core";
import { CfnOutput, Duration } from "aws-cdk-lib";
import { Certificate } from "aws-cdk-lib/aws-certificatemanager";
import {
    Distribution,
    PriceClass,
    ViewerProtocolPolicy
} from "aws-cdk-lib/aws-cloudfront";
import { RestApiOrigin, S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { Effect, PolicyStatement, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import { Bucket } from "aws-cdk-lib/aws-s3";
import {hostingDomain} from "./constants";

interface GraphiqlExplorerProps {
    appName: string;

}

export class GraphiqlExplorer extends Construct {
    constructor(scope: GuStack, id: string, props: GraphiqlExplorerProps) {
        super(scope, id);

        //We can't create the cert here, because it must live in us-east-1 for Cloudfront to use it.
        const hostingCertArn = new GuStringParameter(scope, "ExplorerCertArn", {
            fromSSM: true,
            default: `/${scope.stage}/${scope.stack}/${props.appName}/GlobalCertArn`,
            description: `Cert to use for graphiql ${scope.stage}. This must reside in us-east-1`,
        });
        const certificate = Certificate.fromCertificateArn(this, "CapiExplorerCert", hostingCertArn.valueAsString);

        const staticBucketNameParam = new GuStringParameter(scope, "StaticBucketName", {
            fromSSM: true,
            default: `/account/services/static.serving.bucket`,
            description: "SSM parameter giving the name of a bucket which is to be used for static hosting"
        });

        const hostingBucket = Bucket.fromBucketName(this, "StaticBucket", staticBucketNameParam.valueAsString);

        const distro = new Distribution(scope, "GraphiQLDistro", {
            defaultRootObject: "index.html",
            certificate,
            domainNames: [hostingDomain[scope.stage]],
            defaultBehavior: {
                viewerProtocolPolicy: ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
                origin: new S3Origin(hostingBucket, {
                    originPath: `${scope.stage}/${props.appName}`
                }),
            },
            enableIpv6: true,
            enabled: true,
            priceClass: PriceClass.PRICE_CLASS_100, //US & EU only
            /*
            we must tell Cloudfront to redirect 403 (forbidden/not present) exceptions from S3 into a 200 response from /index in order for react-router to work.
             */
            errorResponses: [
                {
                    httpStatus: 403,
                    responseHttpStatus: 200,
                    responsePagePath: "/index.html",
                    ttl: Duration.seconds(5),
                }
            ]
        });

        hostingBucket.addToResourcePolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            principals: [
                new ServicePrincipal("cloudfront.amazonaws.com"),
            ],
            actions: ["s3:GetObject"],
            resources: [`arn:aws:s3:::${hostingBucket.bucketName}/${scope.stage}/${props.appName}`],
            conditions: {
                "StringEquals": {
                    "AWS:SourceArn": `arn:aws:cloudfront::${scope.account}:distribution/${distro.distributionId}`
                }
            }
        }));

        new CfnOutput(this, "DistroUrlOut", {
            value: distro.distributionDomainName,
        });
    }
}