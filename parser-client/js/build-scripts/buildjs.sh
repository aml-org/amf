#!/bin/bash

cd ./parser-client/js

echo 'SHACLValidator = require("amf-shacl-node")' > amf.js
cat ./target/artifact/parser-client-module.js >> amf.js
chmod a+x amf.js

cd ../..