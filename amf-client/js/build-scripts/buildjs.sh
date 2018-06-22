#!/bin/bash

cd ./amf-client/js

echo 'SHACLValidator = require("amf-shacl-node")' > amf.js
cat ./target/artifact/amf-client-module.js >> amf.js
chmod a+x amf.js

cd ../..