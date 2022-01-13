echo "Running fullOpt"
sbt cliJS/fullOptJS
echo "Finished fullOpt"

# echo "Generating typings"
# sbt -Dsbt.sourcemode=true cliJS/generateTypings
# echo "Finished generating typings"

# echo "Running build-typings script"
# ./amf-cli/js/build-scripts/build-typings.sh
# echo "Finished build-typings script"
