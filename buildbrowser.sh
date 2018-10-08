#!/bin/bash

printf "\n\n\nHACK FOR AMF BROWSER VERSION, run me from the top level directory of the AMF project. YOU NEED SBT AND BROWSERIFY IN YOUR PATH!!!\n\n\n"
printf "\n\n\n**** SCALA BUILDING\n\n\n"
sbt clean
sbt fullOptJS
printf "\n\n\n**** BROWSERIFYING\n\n\n"
rm -f amf.tmp
cp parser-client/js/target/artifact/amf-client-module.js ./amf.tmp
#browserify amf.tmp --standalone amf > amfbrowser.js
#printf "\n\n\n**** CLEANING\n\n\n"
#rm -f amf.tmp
printf "\n\n\n**** CHECK: amfbrowser.js\n\n\n"
