#!/bin/bash

cd ./amf-client/js || exit 1
cp -f  ./target/artifact/@types/amf-client-module.d.ts typings/amf-client-js.d.ts
cd ../..