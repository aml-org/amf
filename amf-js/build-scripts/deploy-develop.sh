#!/usr/bin/env bash

# THIS SCRIPT MUST BE EXECUTED FROM PATH (root)/amf-js AND NOT FROM (root)/amf-js/build-scripts

source ./build-scripts/pre-deploy.sh

echo "Current directory: `pwd`"


export TAG=${TAG_VERSION}


echo "Used target tag=$TAG"

if [[ "${TAG}" == "master" ]]; then
  export DEVELOP_VERSION_BASE=`npm v @mulesoft/amf-js dist-tags.latest`
else
  export DEVELOP_VERSION_BASE=`npm v @mulesoft/amf-js dist-tags.$TAG`
fi

if [[ "$DEVELOP_VERSION_BASE" == "undefined" ]]; then
  DEVELOP_VERSION_BASE='0.0.1-automatic'
fi

if [[ "$DEVELOP_VERSION_BASE" == "" ]]; then
  DEVELOP_VERSION_BASE='0.0.1-automatic'
fi


echo "Base $DEVELOP_VERSION_BASE"
echo "Branch $TAG_VERSION"
echo "-> ${DEVELOP_VERSION_BASE/automatic/$TAG}"
export DEVELOP_VERSION=${DEVELOP_VERSION_BASE/automatic/$TAG}
echo "Targeting $DEVELOP_VERSION"

export NPM_USER=`npm whoami --registry https://npm.mulesoft.com 2> /dev/null`

if [ -z ${NPM_USER} ]; then
    echo 'NPM_USER NOT VALID FOR @mulesoft REGISTRY. CHECK THE ENV-VAR NPM_TOKEN'
    exit 1;
else
    echo "PUBLISHING @${TAG_VERSION} RELEASE AS @${NPM_USER}"
    npm version ${DEVELOP_VERSION} --force --no-git-tag-version
    npm version prerelease --force --no-git-tag-version

    set -e # Break the script if it fails

    if [[ "${TAG}" == "master" ]]; then
      npm publish
    else
      npm publish --tag=${TAG}
    fi
fi
