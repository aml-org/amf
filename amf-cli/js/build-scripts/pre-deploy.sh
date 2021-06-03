#!/usr/bin/env bash

echo "EXECUTING DEPLOY.SH"

if [ "${NPM_TOKEN}" ]; then
    echo "TRYING TO CREATE .npmrc"

    echo "@mulesoft:registry=https://nexus3.build.msap.io/repository/npm-internal/" >> ~/.npmrc
    echo "//nexus3.build.msap.io/repository/npm-internal/:_authToken=${NPM_TOKEN}" >> ~/.npmrc

    echo OK
fi
