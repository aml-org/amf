#!/bin/bash

echo '#!/usr/bin/env node' > amf.js
echo 'SHACLValidator = require(__dirname + "/src/main/resources/shacl_node.js")' >> amf.js
cat ./target/artifact/amf-client-module.js >> amf.js
chmod a+x amf.js
