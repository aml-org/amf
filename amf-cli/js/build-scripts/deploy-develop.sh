#!/usr/bin/env bash

# THIS SCRIPT MUST BE EXECUTED FROM PATH (root) AND NOT FROM (root)/amf-client/js/build-scripts

source ./amf-client/js/build-scripts/buildjs.sh

cd ./amf-client/js

source ./build-scripts/pre-deploy.sh

echo "Current directory: `pwd`"

export DEVELOP_VERSION_BASE=`npm v @mulesoft/amf-client-js dist-tags.latest`

if [[ "$DEVELOP_VERSION_BASE" == "undefined" ]]; then
  DEVELOP_VERSION_BASE='0.0.1-automatic'
fi

if [[ "$DEVELOP_VERSION_BASE" == "" ]]; then
  DEVELOP_VERSION_BASE='0.0.1-automatic'
fi


echo "Base $DEVELOP_VERSION_BASE"
export DEVELOP_VERSION=${DEVELOP_VERSION_BASE/automatic/master}
echo "Targeting $DEVELOP_VERSION"

npm version ${DEVELOP_VERSION} --force --no-git-tag-version
npm version prerelease --force --no-git-tag-version

set -e # Break the script if it fails

npm publish

cd ../..