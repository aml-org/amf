#!/bin/bash

sbt clean
sbt generateJSMainModule
echo '#!/usr/bin/env node' > amf.js
echo 'SHACLValidator = require("./amf-js/src/main/resources/shacl_node.js")' >> amf.js
cat ./amf-js/target/artifact/js-main-module.js >> amf.js
chmod a+x amf.js
