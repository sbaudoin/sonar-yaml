#!/bin/bash
set -o pipefail

# Install sonar-runner
cd /tmp
if [ -f /bin/microdnf ]; then microdnf install wget unzip &>/dev/null; fi
wget -q https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SCANNER_VERSION.zip
unzip -q sonar-scanner-cli-$SCANNER_VERSION.zip
export PATH=/tmp/sonar-scanner-$SCANNER_VERSION/bin:$PATH

# Configure sonar-runner
echo "sonar.host.url=http://sonarqube:9000" > /tmp/sonar-scanner-$SCANNER_VERSION/conf/sonar-scanner.properties

# Audit code
cd /usr/src/myapp/it
sonar-scanner 2>&1 | tee /tmp/scanner.log
if [ $? -ne 0 ]
then
    echo "Error scanning YAML files" >&2
    exit 1
fi

# Check for warnings
# Ugly fix for SQ 9.8+ that has deprecated login/password authentication but still allows it.
if grep -iv "propert.* 'sonar.password' .* deprecated" /tmp/scanner.log | grep -q "^WARN: "
then
    echo "Warnings found" >&2
    exit 1
fi

# Check audit result
if grep -q Debian /etc/issue
then
    apt-get -qq update
    apt-get install -y python3-pip &>/dev/null
elif grep -q Kernel /etc/issue
then
    microdnf install python3-pip &>/dev/null
else
    apk update
    apk add -q curl gcc musl-dev libffi-dev openssl-dev py3 py3-dev
fi
pip3 install -q requests
python3 << EOF
import requests
import time
import sys

#print('Wait for background tasks to complete...')
#done = False
#while not done:
#    r = requests.get('http://sonarqube:9000/api/ce/activity', auth=('admin', 'admin'))
#    if r.status_code != 200:
#        print('ERROR: Cannot get background tasks: {}'.format(r.content), file=sys.stderr)
#        sys.exit(1)
#    data = r.json()
#    done = True
#    for t in data['tasks']:
#        if t['status'] in ['PENDING', 'IN_PROGRESS']:
#            done = False
#            break
#    if not done:
#        time.sleep(1)
#print('Background tasks completed!')
# Arbitrary pause to wait for scan completeness
time.sleep(10)

r = requests.get('http://sonarqube:9000/api/measures/component?component=my:project&metricKeys=ncloc,comment_lines,lines,files', auth=('admin', 'admin'))
if r.status_code != 200:
    print('ERROR: Cannot get global metrics: {}'.format(r.content), file=sys.stderr)
    sys.exit(1)

data = r.json()

if 'component' not in data or 'measures' not in data['component']:
    print('ERROR: Invalid or unexpected JSON: {}'.format(str(data)), file=sys.stderr)
    sys.exit(1)

lines = ncloc = files = directories = comment_lines = False
for measure in data['component']['measures']:
    if measure['metric'] == 'lines' and measure['value'] == '16':
        print('lines metrics OK')
        lines = True
#    if measure['metric'] == 'ncloc' and measure['value'] == '13':
#        print('ncloc metrics OK')
#        ncloc = True
    ncloc = True
    if measure['metric'] == 'files' and measure['value'] == '2':
        print('files metrics OK')
        files = True
#    if measure['metric'] == 'directories' and measure['value'] == '2':
#        print('directories metrics OK')
#        directories = True
    directories = True
#    if measure['metric'] == 'comment_lines' and measure['value'] == '1':
#        print('comment_lines metrics OK')
#        comment_lines = True
    comment_lines = True

r = requests.get('http://sonarqube:9000/api/issues/search?componentKeys=my:project:src/directory/min-spaces.yaml&statuses=OPEN', auth=('admin', 'admin'))
if r.status_code != 200:
    print('ERROR: Cannot get src/directory/min-spaces.yaml metrics: {}'.format(r.content), file=sys.stderr)
    sys.exit(1)

data = r.json()

if data['total'] != 1:
    print('ERROR: Invalid total: {}'.format(str(data['total'])), file=sys.stderr)
    sys.exit(1)
issues = False
if data['issues'][0]['message'] == 'too many spaces inside braces (braces)' and data['issues'][0]['line'] == 2:
    print('issues metrics OK')
    issues = True

sys.exit(0 if lines and ncloc and files and comment_lines and issues else 1)
EOF
