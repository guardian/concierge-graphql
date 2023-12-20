#!/usr/bin/env bash

if [ -d build/ ]; then
  rm -rf build
fi

yarn --frozen-lockfile
yarn run build
docker build . -t graphiql-explorer:DEV