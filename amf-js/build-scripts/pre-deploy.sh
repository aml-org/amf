#!/usr/bin/env bash

npm whoami --registry https://npm.mulesoft.com

echo "EXECUTING DEPLOY.SH"

export NPM_USER=`npm whoami --registry https://npm.mulesoft.com 2> /dev/null`

if [ -z $NPM_USER ]; then
    echo "TRYING TO CREATE .npmrc"

    if [ "${NPM_TOKEN}" ]; then
        echo "@mulesoft:registry=https://npm.mulesoft.com/" >> ~/.npmrc
        echo "//npm.mulesoft.com/:_authToken=${NPM_TOKEN}" >> ~/.npmrc

        echo OK
    else
        echo MISSING NPM_TOKEN
    fi

    export NPM_USER=`npm whoami --registry https://npm.mulesoft.com 2> /dev/null`
fi