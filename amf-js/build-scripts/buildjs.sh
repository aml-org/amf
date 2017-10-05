#!/bin/bash

sbt clean
sbt fullOptJS
echo '#!/usr/bin/env node' > amf.js
echo 'require("./amf-js/src/main/resources/shacl.js")' >> amf.js
cat ./amf-js/target/scala-2.12/amf-opt.js >> amf.js
chmod a+x amf.js
