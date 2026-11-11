import { GuCertificate } from "@guardian/cdk/lib/constructs/acm";
import type { GuStackProps } from "@guardian/cdk/lib/constructs/core";
import { GuStack } from "@guardian/cdk/lib/constructs/core";
import type { App } from "aws-cdk-lib";
import { CfnOutput } from "aws-cdk-lib";

interface GraphiqlCertificateProps extends GuStackProps {
  domainName: string;
}

/**
 * CloudFront viewer certificate, which must be created in us-east-1.
 * DNS validation records are created automatically by dns-validation-lambda.
 *
 * After deploying each certificate stack and confirming the certificate is issued, copy its
 * ARN into the eu-west-1 SSM parameter consumed by the GraphiQL application stack:
 *
 * ```sh
 * STAGE=CODE-AARDVARK
 * DOMAIN=graphiql.code-aardvark.guexperiments.co.uk
 * # Repeat for PROD-AARDVARK, CODE-ZEBRA and PROD-ZEBRA with their matching domains.
 *
 * ARN=$(aws cloudformation describe-stacks \
 *   --profile capi --region us-east-1 \
 *   --stack-name "content-api-$STAGE-graphiql-explorer-certificates" \
 *   --query "Stacks[0].Outputs[?OutputKey=='CertificateArn'].OutputValue | [0]" \
 *   --output text)
 *
 * aws acm describe-certificate \
 *   --profile capi --region us-east-1 \
 *   --certificate-arn "$ARN" \
 *   --query Certificate.Status --output text  # expect ISSUED
 *
 * aws ssm put-parameter \
 *   --profile capi --region eu-west-1 \
 *   --name "/INFRA/$DOMAIN/cloudFrontCertificateArn" \
 *   --type String --value "$ARN" \
 *   --description "CloudFront viewer certificate for GraphiQL ($STAGE), issued in us-east-1"
 * ```
 */
export class GraphiqlCertificate extends GuStack {
  constructor(scope: App, id: string, props: GraphiqlCertificateProps) {
    super(scope, id, {
      description: "GraphiQL CloudFront certificate",
      ...props,
    });

    const certificate = new GuCertificate(this, {
      app: "graphiql-explorer-certificates",
      domainName: props.domainName,
    });

    new CfnOutput(this, "CertificateArn", {
      description: `CloudFront certificate for ${props.domainName}`,
      value: certificate.certificateArn,
    });
  }
}