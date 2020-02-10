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

@Rule(key = "CommasCheck")
public class CommasCheck extends YamlLintCheck {
    @RuleProperty(key = "max-spaces-before", description = "Maximal number of spaces allowed before commas (use -1 to disable)", defaultValue = "0")
    int maxSpacesBefore;

    @RuleProperty(key = "min-spaces-after", description = "Minimal number of spaces after commas", defaultValue = "1")
    int minSpacesAfter;

    @RuleProperty(key = "max-spaces-after", description = "Maximal number of spaces allowed after commas (use -1 to disable)", defaultValue = "1")
    int maxSpacesAfter;
}
