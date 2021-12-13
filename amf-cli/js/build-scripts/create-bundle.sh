#!/bin/bash

cd ./amf-cli/js || exit

echo 'globalThis.Ajv = require("ajv")' > amf.js || exit
cat ./target/artifact/amf-client-module.js >> amf.js || exit
chmod a+x amf.js

cd ../..
