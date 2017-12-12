#!/usr/bin/env bash

# THIS SCRIPT MUST BE EXECUTED FROM PATH (root) AND NOT FROM (root)/amf-client/js/build-scripts

cd ./amf-client/js

source ./build-scripts/pre-deploy.sh

cd src/main/resources

echo "Current directory: `pwd`"

export DEVELOP_VERSION_BASE=`npm v @mulesoft/amf-shacl-node dist-tags.latest`

if [[ "$DEVELOP_VERSION_BASE" == "undefined" ]]; then
  DEVELOP_VERSION_BASE='0.0.1-automatic'
fi

if [[ "$DEVELOP_VERSION_BASE" == "" ]]; then
  DEVELOP_VERSION_BASE='0.0.1-automatic'
fi


echo "Base $DEVELOP_VERSION_BASE"
export DEVELOP_VERSION=${DEVELOP_VERSION_BASE/automatic/master}
echo "Targeting $DEVELOP_VERSION"

export NPM_USER=`npm whoami --registry https://nexus3.build.msap.io/repository/npm-internal 2> /dev/null`

if [ -z ${NPM_USER} ]; then
    echo 'NPM_USER NOT VALID FOR @mulesoft REGISTRY. CHECK THE ENV-VAR NPM_TOKEN'
    cd ..
    exit 1;
else
    echo "PUBLISHING @master RELEASE AS @${NPM_USER}"
    npm version ${DEVELOP_VERSION} --force --no-git-tag-version
    npm version prerelease --force --no-git-tag-version

    set -e # Break the script if it fails

    npm publish
fi

cd ../../../../..