#!/bin/bash

# Get Maven project version
MAVEN_VERSION=$(grep '<version>' pom.xml | head -1 | sed 's/<\/\?version>//g'| awk '{print $1}')

# Deploy only if tag or master branch and SNAPSHOT version
if [[ -n "$TRAVIS_TAG" || ( "$TRAVIS_BRANCH" = "master" && "$MAVEN_VERSION" =~ -SNAPSHOT$ ) ]]
then
    echo deploy
fi
