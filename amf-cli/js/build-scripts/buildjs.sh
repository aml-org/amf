#!/bin/bash

# cd ./amf-cli/js

echo 'SHACLValidator = require("amf-shacl-node")' > amf.js
echo 'Ajv = require("ajv")' >> amf.js
cat ./target/artifact/amf-client-module.js >> amf.js
chmod a+x amf.js

cd ../..
