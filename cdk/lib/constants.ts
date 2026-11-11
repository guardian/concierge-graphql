import { graphiqlDomains } from "./graphiql-domains";

export const hostingDomain:Record<string,string> = {
    ...graphiqlDomains,
    "TEST": "graphiql.test.guexperiments.co.uk"
};