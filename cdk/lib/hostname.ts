import {GuStack} from "@guardian/cdk/lib/constructs/core";

export function getHostName(scope:GuStack):string {
    if(scope.stage.startsWith("CODE")) {
        if(scope.stack.endsWith("preview")) {
            return "concierge-graphql-preview.content.code.dev-guardianapis.com";
        } else {
            return "concierge-graphql.content.code.dev-guardianapis.com";
        }
    } else if(scope.stage.startsWith("PROD")) {
        if (scope.stack.endsWith("preview")) {
            return "concierge-graphql-preview.content.guardianapis.com";
        } else {
            return "concierge-graphql.content.guardianapis.com";
        }
    } else if(scope.stage=="TEST") {    //CI testing
        return "concierge-graphql-preview.content.code.dev-guardianapis.com";
    } else {
        throw "stage must be either CODE or PROD";
    }
}