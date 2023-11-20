<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
YAML SonarQube Plugin
=====================

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)](http://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.sbaudoin/sonar-yaml-plugin.svg?label=Maven%20Central)](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.sbaudoin%22%20AND%20a%3A%22sonar-yaml-plugin%22)
[![Build Status](https://travis-ci.org/sbaudoin/sonar-yaml.svg?branch=master)](https://travis-ci.org/sbaudoin/sonar-yaml)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=sbaudoin_sonar-yaml&metric=alert_status)](https://sonarcloud.io/dashboard?id=sbaudoin_sonar-yaml)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=sbaudoin_sonar-yaml&metric=coverage)](https://sonarcloud.io/dashboard?id=sbaudoin_sonar-yaml)

SonarQube plugin to analyze YAML code based on [yamllint](https://github.com/sbaudoin/yamllint).

## Rules

The plugin comes with a default "Sonar way" profile with most common rules enabled:

* Anchors check
* Syntax error check
* Braces check
* Brackets check
* Colons check
* Commas check
* Comments check
* Comments indentation check
* Document start check
* Empty lines check
* Empty values check
* Hyphens check
* Indentation check
* Key duplicates check
* Line length check
* New line at end of file check
* New lines check
* Octal values check
* Trailing spaces check
* Truthy check

Some additional rules are provided but not enabled by default:

* Document end check
* Float values check
* Forbidden key check (template)
* Forbidden value check (template, new in 1.3.0)
* Key ordering check
* Quoted strings check (new in 1.4.0)
* Required key check (template, new in 1.5.0)
* Int value in range check (template, new in 1.8.0)

Once installed, you may go to the profile management screens to create your own profile and add or remove rules, change levels, and parameters, etc.

## Installation

Plugin for SonarQube 8.1+ (including 8.9 LTS), 9.0+ (including SonarQube 9.2 as of version 1.7 and 9.9 LTS), 10.0+ (tested on 10.2 only).

Just [download the plugin JAR file](https://github.com/sbaudoin/sonar-yaml/releases) and copy it to the `extensions/plugins` directory of SonarQube and restart.

## Ancestors rule properties

Version 1.8.0 introduces included-ancestors and excluded-ancestors as regex rule properties, for the following template checks:
1. forbidden key
2. forbidden value
3. required key and
4. int value in range

This provides the possibility to apply the checks _only_ in a certain scope 1 and/or _only not_ in a certain scope 2. Current limitation: yaml list notation is not supported by ancestor matching.

## Troubleshooting/known issues

### Scan fails with "ERROR: Caused by: _x_ is not a valid line offset for pointer. File _xyz.yml_ has _y_ character(s) at line _z_"

This may be due to [issue #6](https://github.com/sbaudoin/sonar-yaml/issues/6): if your YAML file contains YAML-valid UTF-8 line break characters such as U+2028, SonarQube
may just strip them, causing the character and lines references being different between the YAML parser and SonarQube.

If such an error is met, go to the main or project general settings of the YAML plugin and enable the option "Filter UTF-8 Line Breaks".
This will make the plugin to ignore some valid UTF-8 line break characters (U+2028, U+2029 and U+0085) so that SonarQube and the plugin
both use the same character and line indices and, the scan should complete.

### SonarQube 9.2 no longer starts after installing the plugin

This is due to the fact that SonarQube 9.2 has brought native support to the YAML language. See [PR #58](https://github.com/sbaudoin/sonar-yaml/pull/58)
and [issue #63](https://github.com/sbaudoin/sonar-yaml/issues/63). To fix the issue, please install the plugin version 1.7+

### Scan fails with "java.lang.UnsupportedOperationException: Can not add the same measure twice"

This is due to the fact that some other plugin has already saved measures for the YAML files. See [issue #70](https://github.com/sbaudoin/sonar-yaml/issues/70).
This issue is fixed with version 1.8.0. If you cannot upgrade, you must disable this plugin or the other plugins that scan YAML files.
