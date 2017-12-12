#!/usr/bin/env bash

echo "EXECUTING DEPLOY.SH"

export NPM_USER=`npm whoami --registry https://nexus3.build.msap.io/repository/npm-internal 2> /dev/null`

if [ -z $NPM_USER ]; then
    echo "TRYING TO CREATE .npmrc"

    if [ "${NPM_TOKEN}" ]; then
        echo "@mulesoft:registry=https://nexus3.build.msap.io/repository/npm-internal" >> ~/.npmrc
        echo "//nexus3.build.msap.io/repository/npm-internal/:_authToken=${NPM_TOKEN}" >> ~/.npmrc

        echo OK
    else
        echo MISSING NPM_TOKEN
    fi

    export NPM_USER=`npm whoami --registry https://nexus3.build.msap.io/repository/npm-internal 2> /dev/null`
fi