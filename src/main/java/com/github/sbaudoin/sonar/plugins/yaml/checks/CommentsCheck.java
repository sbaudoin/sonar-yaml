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

@Rule(key = "CommentsCheck")
public class CommentsCheck extends YamlLintCheck {
    @RuleProperty(key = "ignore-shebangs", description = "Ignore a shebang at the beginning of the file when 'require-starting-space' is set", defaultValue = "false")
    boolean ignoreShebangs;

    @RuleProperty(key = "require-starting-space", description = "Tells if a space character is required right after the # or not", defaultValue = "true")
    boolean requireStartingSpace;

    @RuleProperty(key = "min-spaces-from-content", description = "Minimal required number of spaces between a comment and its preceding content for inline comments", defaultValue = "2")
    int minSpacesFromContent;
}
