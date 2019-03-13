/**
 * Copyright (c) 2018-2019, Sylvain Baudoin
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

@Rule(key = "BracketsCheck")
public class BracketsCheck extends YamlLintCheck {
    @RuleProperty(key = "min-spaces-inside", description = "Minimal number of spaces required inside brackets", defaultValue = "0")
    int minSpacesInside;

    @RuleProperty(key = "max-spaces-inside", description = "Maximal number of spaces required inside brackets", defaultValue = "0")
    int maxSpacesInside;

    @RuleProperty(key = "min-spaces-inside-empty", description = "Minimal number of spaces required inside empty brackets", defaultValue = "-1")
    int minSpacesInsideEmpty;

    @RuleProperty(key = "max-spaces-inside-empty", description = "Maximal number of spaces required inside empty brackets", defaultValue = "-1")
    int maxSpacesInsideEmpty;
}
