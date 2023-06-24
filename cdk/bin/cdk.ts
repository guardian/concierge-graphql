import "source-map-support/register";
import { App } from "aws-cdk-lib";
import { ConciergeGraphql } from "../lib/concierge-graphql";

const app = new App();
new ConciergeGraphql(app, "ConciergeGraphql-PROD", { stack: "content-api", stage: "PROD" });
new ConciergeGraphql(app, "ConciergeGraphql-CODE", { stack: "content-api", stage: "CODE" });
