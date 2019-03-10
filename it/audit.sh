#!/bin/bash

# Install sonar-runner
cd /tmp
wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SCANNER_VERSION-linux.zip
unzip -q sonar-scanner-cli-$SCANNER_VERSION-linux.zip
export PATH=/tmp/sonar-scanner-$SCANNER_VERSION-linux/bin:$PATH

# Configure sonar-runner
echo "sonar.host.url=http://sonarqube:9000" > /tmp/sonar-scanner-$SCANNER_VERSION-linux/conf/sonar-scanner.properties

# Audit code
cd /usr/src/myapp/it
sonar-scanner
if [ $? -ne 0 ]
then
    echo "Error scanning YAML files" >&2
    exit 1
fi

# Check audit result
apt-get -qq update
apt-get -qq install -y python-pip
pip install -q requests
python << EOF
import requests
import sys

r = requests.get('http://sonarqube:9000/api/measures/component?component=my:project&metricKeys=ncloc,comment_lines,lines,files,directories', auth=('admin', 'admin'))
if r.status_code != 200:
    sys.exit(1)

data = r.json()

if 'component' not in data or 'measures' not in data['component']:
    sys.exit(1)

lines = ncloc = files = directories = comment_lines = False
for measure in data['component']['measures']:
    if measure['metric'] == 'lines' and measure['value'] == '16':
        print 'lines metrics OK'
        lines = True
    if measure['metric'] == 'ncloc' and measure['value'] == '13':
        print 'ncloc metrics OK'
        ncloc = True
    if measure['metric'] == 'files' and measure['value'] == '2':
        print 'files metrics OK'
        files = True
    if measure['metric'] == 'directories' and measure['value'] == '2':
        print 'directories metrics OK'
        directories = True
    if measure['metric'] == 'comment_lines' and measure['value'] == '1':
        print 'comment_lines metrics OK'
        comment_lines = True

r = requests.get('http://sonarqube:9000/api/issues/search?componentKeys=my:project:src/directory/min-spaces.yaml&statuses=OPEN', auth=('admin', 'admin'))
if r.status_code != 200:
    sys.exit(1)

data = r.json()

if data['total'] != 1:
    sys.exit(1)
issues = False
if data['issues'][0]['message'] == 'too many spaces inside braces (braces)' and data['issues'][0]['line'] == 2:
    print 'issues metrics OK'
    issues = True

sys.exit(0 if lines and ncloc and files and directories and comment_lines and issues else 1)
EOF
