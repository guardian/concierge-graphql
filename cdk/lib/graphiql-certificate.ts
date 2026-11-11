import { GuCertificate } from "@guardian/cdk/lib/constructs/acm";
import type { GuStackProps } from "@guardian/cdk/lib/constructs/core";
import { GuStack } from "@guardian/cdk/lib/constructs/core";
import type { App } from "aws-cdk-lib";
import { CfnOutput } from "aws-cdk-lib";

interface GraphiqlCertificateProps extends GuStackProps {
  domainName: string;
}

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