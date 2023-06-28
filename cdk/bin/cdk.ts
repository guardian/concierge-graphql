import "source-map-support/register";
import { App } from "aws-cdk-lib";
import { ConciergeGraphql } from "../lib/concierge-graphql";

const app = new App();
new ConciergeGraphql(app, "ConciergeGraphql-PROD-AARDVARK", { stack: "content-api", stage: "PROD-AARDVARK" });
new ConciergeGraphql(app, "ConciergeGraphql-preview-PROD-AARDVARK", { stack: "content-api-preview", stage: "PROD-AARDVARK" });
new ConciergeGraphql(app, "ConciergeGraphql-CODE-AARDVARK", { stack: "content-api", stage: "CODE-AARDVARK" });
new ConciergeGraphql(app, "ConciergeGraphql-preview-CODE-AARDVARK", { stack: "content-api-preview", stage: "CODE-AARDVARK" });

new ConciergeGraphql(app, "ConciergeGraphql-PROD-ZEBRA", { stack: "content-api", stage: "PROD-ZEBRA" });
new ConciergeGraphql(app, "ConciergeGraphql-preview-PROD-ZEBRA", { stack: "content-api-preview", stage: "PROD-ZEBRA" });
new ConciergeGraphql(app, "ConciergeGraphql-CODE-ZEBRA", { stack: "content-api", stage: "CODE-ZEBRA" });
new ConciergeGraphql(app, "ConciergeGraphql-preview-CODE-ZEBRA", { stack: "content-api-preview", stage: "CODE-ZEBRA" });