import {GuStack} from "@guardian/cdk/lib/constructs/core";

export function getHostName(scope:GuStack):string {
    if(scope.stage=="CODE") {
        if(scope.stack.endsWith("preview")) {
            return "concierge-graphql-preview.capi.code.dev-gutools.co.uk";
        } else {
            return "concierge-graphql.capi.code.dev-gutools.co.uk";
        }
    } else if(scope.stage=="PROD") {
        if (scope.stack.endsWith("preview")) {
            return "concierge-graphql-preview.capi.gutools.co.uk";
        } else {
            return "concierge-graphql.capi.gutools.co.uk";
        }
    } else if(scope.stage=="TEST") {    //CI testing
        return "concierge-graphql-preview.capi.code.dev-gutools.co.uk";
    } else {
        throw "stage must be either CODE or PROD";
    }
}