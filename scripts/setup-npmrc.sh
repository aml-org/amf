#!/usr/bin/env bash

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_SCRIPT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

source ./scripts/navigate-to-amf-cli.sh

echo "Connecting to NPM @aml-org org"
printf "@aml-org:registry=https://registry.npmjs.org/\n//registry.npmjs.org/:_authToken=$NPM_TOKEN" > .npmrc
touch .npmrc

cd $BASE_SCRIPT_DIR
