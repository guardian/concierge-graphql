import {GuStack} from "@guardian/cdk/lib/constructs/core";

export function getHostName(scope:GuStack, insert?:string):string {
    if(scope.stage.startsWith("CODE")) {
        if(scope.stack.endsWith("preview")) {
            return `graphql-preview${insert ?? ""}.content.code.dev-guardianapis.com`;
        } else {
            return `graphql${insert ?? ""}.content.code.dev-guardianapis.com`;
        }
    } else if(scope.stage.startsWith("PROD")) {
        if (scope.stack.endsWith("preview")) {
            return `graphql-preview${insert ?? ""}.content.guardianapis.com`;
        } else {
            return `graphql${insert ?? ""}.content.guardianapis.com`;
        }
    } else if(scope.stage=="TEST") {    //CI testing
        return `graphql-preview${insert ?? ""}.content.code.dev-guardianapis.com`;
    } else {
        throw "stage must be either CODE or PROD";
    }
}