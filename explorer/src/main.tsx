import React from 'react';
import ReactDOM from 'react-dom';
import {Helmet} from 'react-helmet';

import {LoginForm} from "./LoginForm";

const rootElem = document.createElement('div');
rootElem.setAttribute("style","height: 100vh");

document.body.append(rootElem)
ReactDOM.render(
    <>
        <Helmet>
            <title>CAPI GraphQL Experiments</title>
        </Helmet>
        <LoginForm />
    </>,
    rootElem,
);
