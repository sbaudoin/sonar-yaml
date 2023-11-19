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

/**
 * Check for float values
 */
@Rule(key = "FloatValuesCheck")
public class FloatValuesCheck extends YamlLintCheck {
    @RuleProperty(key = "require-numeral-before-decimal", description = "Require floats to start with a numeral (ex \"0.0\" instead of \".0\")", defaultValue = "false")
    boolean requireNumeralBeforeDecimal;

    @RuleProperty(key = "forbid-scientific-notation", description = "Forbid scientific notation", defaultValue = "false")
    boolean forbidScientificNotation;

    @RuleProperty(key = "forbid-nan", description = "Forbid NaN (not a number) values", defaultValue = "false")
    boolean forbidNan;

    @RuleProperty(key = "forbid-inf", description = "Forbid infinite values", defaultValue = "false")
    boolean forbidInf;
}
