#!/bin/bash

cd ./amf-client/js

echo 'globalThis.SHACLValidator = require("amf-shacl-node")' > amf.js
echo 'globalThis.Ajv = require("ajv")' >> amf.js
cat ./target/artifact/amf-client-module.js >> amf.js
chmod a+x amf.js

cd ../..
