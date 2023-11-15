#!/bin/bash

# Get Maven project version
MAVEN_VERSION=$(grep '<version>' pom.xml | head -1 | sed 's/<\/\?version>//g'| awk '{print $1}')
echo "Maven version: $MAVEN_VERSION"
echo "Travis branch: $TRAVIS_BRANCH"
echo "Travis tag: $TRAVIS_TAG"

# Deploy only if tag or master branch and SNAPSHOT version
if [[ -n "$TRAVIS_TAG" || ( "$TRAVIS_BRANCH" = "master" && "$MAVEN_VERSION" =~ -SNAPSHOT$ ) ]]
then
    echo "Deploying..."
    export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"
    mvn --settings .travis/settings.xml deploy -DskipTests=true -B -V
fi
