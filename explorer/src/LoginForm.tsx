import React, {useEffect, useMemo, useState} from "react";
import {GraphiQL} from "graphiql";
import { createGraphiQLFetcher, Fetcher } from '@graphiql/toolkit';
import {css} from "@emotion/react";
import 'graphiql/graphiql.css';
import {Button, TextInput} from "@guardian/source-react-components";

const loginContainer = css`
    margin: auto;
    width: fit-content;
    height: fit-content;
    display: flex;
    flex-direction: column;
`;

const loginElement = css`
    align-self: center;
`
const inputStyle = css`
    max-width: 800px;
    width: 800px;
`;

const buttonContainer = css`
    display: flex;
    margin-top: 1em;
    width: 100%;
    flex-direction: row;
    justify-content: space-evenly;
`;

export const LoginForm:React.FC = () => {
    const defaultBaseUrl = localStorage.getItem("CapiGQLBase") ?? "https://";
    const defaultApiKey = localStorage.getItem("CapiGQLKey") ?? "";
    const [haveCachedLogin, setHaveCachedLogin] = useState(!!(localStorage.getItem("CapiGQLBase") && localStorage.getItem("CapiGQLKey")));

    const [baseUrl, setBaseUrl] = useState(defaultBaseUrl);
    const [apiKey, setApiKey] = useState(defaultApiKey);
    const [readyToGo, setReadyToGo] = useState(false);
    const [urlIsValid, setUrlIsValid] = useState(false);
    const urlValidator = /^https?:\/\/[\w-:.]+$/;

    useEffect(() => {
        setUrlIsValid(urlValidator.test(baseUrl));
    }, [baseUrl]);

    const clearCache = () => {
        localStorage.removeItem("CapiGQLBase");
        localStorage.removeItem("CapiGQLKey");
        setBaseUrl("https://");
        setApiKey("");
        setHaveCachedLogin(false);
    }

    const fetcher = useMemo<Fetcher|null>(()=>{
        if(readyToGo) {
            localStorage.setItem("CapiGQLBase", baseUrl);
            localStorage.setItem("CapiGQLKey", apiKey);

            return createGraphiQLFetcher({url: `${baseUrl}/query`, headers: {'X-Api-Key': apiKey}});
        } else {
            return null;
        }
    }, [readyToGo]);


    return fetcher && readyToGo ?
        <GraphiQL fetcher={fetcher} /> :
        <div css={loginContainer}>
            <div css={loginElement}>
                <h1>Please log in</h1>
            </div>
            <div css={loginElement}>
            <TextInput label="CAPI GraphQL base URL"
                       css={inputStyle}
                       value={baseUrl}
                       error={urlIsValid ? undefined : "Please input only a base URL with no trailing slash, i.e. https://my.server.name"}
                       onChange={(evt)=>setBaseUrl(evt.target.value)}/>
            </div>
            <div css={loginElement}>
            <TextInput label="API key"
                       css={inputStyle}
                       value={apiKey}
                       type="password"
                       onChange={(evt)=>setApiKey(evt.target.value)}
                       />
            </div>
                <div css={buttonContainer}>
                    <div>
                    <Button disabled={!urlIsValid || apiKey==""}
                            onClick={()=>setReadyToGo(true)}>
                        Connect
                    </Button>
                    </div>
                    <div>
                    <Button disabled={!haveCachedLogin}
                            priority="secondary"
                            onClick={clearCache}>
                        Clear</Button>
                    </div>
                </div>
        </div>
}