import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { GraphiqlCertificate } from "./graphiql-certificate";
import { graphiqlDomains, type GraphiqlStage } from "./graphiql-domains";

describe("The GraphiqlCertificate stack", () => {
  it.each(Object.entries(graphiqlDomains))(
    "matches the %s snapshot",
    (stage, domainName) => {
      const app = new App();
      const stack = new GraphiqlCertificate(
        app,
        `GraphiqlCertificate-useast-1-${stage}`,
        {
          stack: "content-api",
          stage: stage as GraphiqlStage,
          env: { region: "us-east-1" },
          domainName,
        },
      );

      expect(Template.fromStack(stack).toJSON()).toMatchSnapshot();
    },
  );
});