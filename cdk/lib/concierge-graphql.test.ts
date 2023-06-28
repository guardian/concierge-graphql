import { App } from "aws-cdk-lib";
import { Template } from "aws-cdk-lib/assertions";
import { ConciergeGraphql } from "./concierge-graphql";

describe("The ConciergeGraphql stack", () => {
  it("matches the snapshot", () => {
    const app = new App();
    const stack = new ConciergeGraphql(app, "ConciergeGraphql", { stack: "content-api", stage: "TEST" });
    const template = Template.fromStack(stack);
    expect(template.toJSON()).toMatchSnapshot();
  });
});
