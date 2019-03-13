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

@Rule(key = "IndentationCheck")
public class IndentationCheck extends YamlLintCheck {
    @RuleProperty(key = "spaces", description = "The indentation width, in spaces", defaultValue = "consistent")
    String spaces;

    @RuleProperty(key = "indent-sequences", description = "Tells whether block sequences should be indented or not", defaultValue = "true")
    String indentSequences;

    @RuleProperty(key = "check-multi-line-strings", description = "Tells whether to lint indentation in multi-line strings", defaultValue = "false")
    boolean checkMultiLineStrings;
}
