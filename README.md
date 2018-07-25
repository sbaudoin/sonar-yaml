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
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-yaml-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-yaml-plugin)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.sbaudoin:sonar-yaml-plugin&metric=coverage)](https://sonarcloud.io/dashboard?id=com.github.sbaudoin:sonar-yaml-plugin)

SonarQube plugin to analyze YAML code based on [yamllint](https://github.com/sbaudoin/yamllint).

## Rules

The plugin comes with a default "Sonar way" profile with most common rules enabled:

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

Once installed, go to the profile management screens to create your own profile and add or remove rules, change levels, and parameters, etc.

## Installation

Plugin for SonarQube 6.6+

Just download the plugin JAR file and copy it to the `extensions/plugins` directory of SonarQube and restart.
