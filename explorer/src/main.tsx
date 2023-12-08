import { createGraphiQLFetcher } from '@graphiql/toolkit';
import { GraphiQL } from 'graphiql';
import React from 'react';
import ReactDOM from 'react-dom';

import 'graphiql/graphiql.css';

const fetcher = createGraphiQLFetcher({ url: 'http://concierge-graphql.test:8000/query', headers: {'X-Consumer-Username': ":internal"} });

const rootElem = document.createElement('div');
rootElem.setAttribute("style","height: 100vh");

document.body.append(rootElem)
ReactDOM.render(
    <GraphiQL fetcher={fetcher} />,
    rootElem,
);
