/**
 * Copyright (c) 2018-2020, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "BracesCheck")
public class BracesCheck extends YamlLintCheck {
    @RuleProperty(key = "forbid", description = "Forbid the use of flow mappings which are denoted by surrounding braces ('{' and '}')", defaultValue = "false")
    boolean forbid;

    @RuleProperty(key = "min-spaces-inside", description = "Minimal number of spaces required inside braces", defaultValue = "0")
    int minSpacesInside;

    @RuleProperty(key = "max-spaces-inside", description = "Maximal number of spaces required inside braces", defaultValue = "0")
    int maxSpacesInside;

    @RuleProperty(key = "min-spaces-inside-empty", description = "Minimal number of spaces required inside empty braces", defaultValue = "-1")
    int minSpacesInsideEmpty;

    @RuleProperty(key = "max-spaces-inside-empty", description = "Maximal number of spaces required inside empty braces", defaultValue = "-1")
    int maxSpacesInsideEmpty;
}
