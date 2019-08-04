#!/bin/bash

export SONARQUBE_VERSION="$1"
export SCANNER_VERSION="$2"
export JAVA_VERSION="$3"
if [ -z "$SCANNER_VERSION" ]
then
    echo "Missing parameters: <SonarQube version> <scanner version>" >&2
    exit 1
fi

export SCRIPT_DIR=`dirname $0`

# Clean-up if needed
echo "Cleanup..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml down

# Start containers
echo "Starting SonarQube..."
#CONTAINER_NAME=`docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d sonarqube 2>&1 | grep -o '[^ ]*sonarqube[^ ]*' | head -1`
docker-compose -f $SCRIPT_DIR/docker-compose.yml up -d sonarqube
CONTAINER_NAME=it_sonarqube_1
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Copy the plugin
MAVEN_VERSION=$(grep '<version>' $SCRIPT_DIR/../pom.xml | head -1 | sed 's/<\/\?version>//g'| awk '{print $1}')
docker cp $SCRIPT_DIR/../target/sonar-yaml-plugin-$MAVEN_VERSION.jar $CONTAINER_NAME:/opt/sonarqube/extensions/plugins
# Restart SonarQube
docker-compose -f $SCRIPT_DIR/docker-compose.yml restart sonarqube
# Wait for SonarQube to be up
grep -q "SonarQube is up" <(docker logs --follow --tail 0 $CONTAINER_NAME)
# Check plug-in installation
if ! docker exec $CONTAINER_NAME curl -su admin:admin http://localhost:9000/api/plugins/installed | python -c '
import sys
import json

data = json.loads(sys.stdin.read())
if "plugins" in data:
    for plugin in data["plugins"]:
        if plugin["key"] == "yaml":
            sys.exit(0)
sys.exit(1)
'
then
    echo "Plugin not installed" >&2
    exit 1
fi

# Audit code
echo "Audit YAML test code..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml up --build --exit-code-from auditor auditor
AUDIT_STATUS=$?

# Delete containers
echo "Cleanup..."
docker-compose -f $SCRIPT_DIR/docker-compose.yml down

exit $AUDIT_STATUS
