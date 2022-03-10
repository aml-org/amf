#!/usr/bin/env bash

DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_DIR="$(cd "${DIR}/.." && pwd)"
DIST_DIR=$BASE_DIR/amf-cli/js

cd $DIST_DIR
