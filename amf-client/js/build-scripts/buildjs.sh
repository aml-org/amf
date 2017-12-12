#!/bin/bash

cd ./amf-client/js

echo '#!/usr/bin/env node' > amf.js
echo 'SHACLValidator = require("@mulesoft/amf-shacl-node")' >> amf.js
cat ./target/artifact/amf-client-module.js >> amf.js
chmod a+x amf.js

cd ../..