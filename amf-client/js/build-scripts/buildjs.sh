#!/bin/bash

cd ./amf-client/js

cat ./src/main/resources/shacl.js > amf.js
#echo 'SHACLValidator = require("amf-shacl-node")' > amf.js
echo 'Ajv = require("ajv")' >> amf.js
cat ./target/artifact/amf-client-module.js >> amf.js
chmod a+x amf.js

cd ../..