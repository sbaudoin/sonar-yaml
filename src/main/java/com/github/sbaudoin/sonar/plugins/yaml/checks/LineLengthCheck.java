/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
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

@Rule(key = "LineLengthCheck")
public class LineLengthCheck extends YamlLintCheck {
    @RuleProperty(key = "max", description = "Maximal (inclusive) length of lines", defaultValue = "80")
    int max;

    @RuleProperty(key = "allow-non-breakable-words", description = "Allows or not non breakable words (without spaces inside) to overflow the limit", defaultValue = "true")
    boolean allowNonBreakableWords;

    @RuleProperty(key = "allow-non-breakable-inline-mappings", description = "Implies allow-non-breakable-words and extends it to also allow non-breakable words in inline mappings", defaultValue = "false")
    boolean allowNonBreakableInlineMappings;
}
