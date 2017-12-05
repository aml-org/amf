#!/bin/bash

sbt clean
sbt clientJS/fullOptJS
echo '#!/usr/bin/env node' > amf.js
echo 'SHACLValidator = require("./amf-client/js/src/main/resources/shacl_node.js")' >> amf.js
cat ./amf-client/js/target/artifact/js-main-module.js >> amf.js
chmod a+x amf.js
