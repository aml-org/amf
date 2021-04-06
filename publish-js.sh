#!/usr/bin/env bash

PROPERTY_FILE=amf-webapi.versions

function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat ${PROPERTY_FILE} | grep "amf.webapi" | cut -d'=' -f2`
   echo ${PROP_VALUE}
}

echo "Reading property version from $PROPERTY_FILE"
PROJECT_VERSION=$(getProperty "nexus.repository.url")

if [[ ${PROJECT_VERSION} == *-SNAPSHOT ]]; then
    IS_SNAPSHOT=true
else
    IS_SNAPSHOT=false
fi

if [[ ${PROJECT_VERSION} == *-RC.* ]]; then
    IS_RC=true
else
    IS_RC=false
fi

echo "amf-webapi.versions version: $PROJECT_VERSION"
echo "Is snapshot: $IS_SNAPSHOT"
echo "Is RC: $IS_RC"

echo "Running fullOpt"
sbt clientJS/fullOptJS
echo "Finished fullOpt"

echo "Running buildjs script"
./amf-client/js/build-scripts/buildjs.sh
echo "Finished buildjs script"

echo "Generating typings"
sbt -Dsbt.sourcemode=true clientJS/generateTypings
echo "Finished generating typings"

echo "Running build-typings script"
./amf-client/js/build-scripts/build-typings.sh
echo "Finished build-typings script"

cd amf-client/js

if ${IS_SNAPSHOT}; then
    LATEST_SNAPSHOT=`npm v amf-client-js dist-tags.snapshot`

    echo "Repo latest snapshot: $LATEST_SNAPSHOT"

    if [[ ${LATEST_SNAPSHOT} == ${PROJECT_VERSION}* ]]; then
        echo "Just add one prerelease"
        npm version ${LATEST_SNAPSHOT} --force --no-git-tag-version
        npm version prerelease --force --no-git-tag-version

        echo "Publish one more snapshot"
    else
        echo "Start prerelease from scratch"
        npm version ${PROJECT_VERSION} --force --no-git-tag-version
        npm version prerelease --force --no-git-tag-version

        echo "Publish new snapshot"
    fi

    echo "NOTE: If this step fails, check that the tag must be wrong and it's trying to publish an existing snapshot!"

    npm publish --tag snapshot

    echo "Finished snapshot publish"
    echo "Add 'beta' tag to snapshot"

    # NEW_VERSION=`npm view amf-client-js@snapshot version`
    # Extract version from package.json because npm doesn't refresh as fast as we need it to.
    NEW_VERSION=`node -p "require('./package.json').version"`

    echo "To version: $NEW_VERSION"

    npm dist-tag add amf-client-js@${NEW_VERSION} beta
else
    if ${IS_RC}; then
        echo "Publishing new RC"
        echo "NOTE: no intelligence here, just publishes the RC version, make sure it does not exist."

        npm version ${PROJECT_VERSION} --force --no-git-tag-version
        npm publish --tag rc

        echo "Finished RC publish"
    else
        LATEST_RELEASE=`npm v amf-client-js dist-tags.latest`
        if [[ ${PROJECT_VERSION} != ${LATEST_RELEASE} ]]; then
            echo "New release $PROJECT_VERSION"
            npm version ${PROJECT_VERSION} --force --no-git-tag-version

            echo "Publish new release"
        else
            echo "Latest release is already $PROJECT_VERSION"
        fi

        npm publish

        echo "Finished latest publish"
        echo "Add 'release' tag to latest"

        npm dist-tag add amf-client-js@${PROJECT_VERSION} release
    fi
fi

# Reset package.json so that the new version is not pushed
git checkout package.json

cd ../..