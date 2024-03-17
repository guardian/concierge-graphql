import React from 'react';
import ReactDOM from 'react-dom';

import {LoginForm} from "./LoginForm";

const rootElem = document.createElement('div');
rootElem.setAttribute("style","height: 100vh");

document.body.append(rootElem)
ReactDOM.render(
    <LoginForm />,
    rootElem,
);
