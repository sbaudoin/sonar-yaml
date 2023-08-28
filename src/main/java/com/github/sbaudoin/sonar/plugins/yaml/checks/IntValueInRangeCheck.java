/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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

import com.github.sbaudoin.yamllint.LintScanner;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.snakeyaml.tokens.ValueToken;

/**
 * Check to be used that the YAML file does not contain values out of the specified range
 */
@Rule(key = "IntValueInRangeCheck")
public class IntValueInRangeCheck extends ForbiddenCheck {
    @RuleProperty(key = "minValue", description = "Minimum value")
    int minValue;

    @RuleProperty(key = "maxValue", description = "Maximum value")
    int maxValue;

    /**
     * Takes the next token and, if it is a key that matches the {@code key-name} regex, analyzes its value against the
     * {@code value} regex, possibly returning an issue if there is a match
     *
     * @param parser the scanner that holds the tokens
     */
    @Override
    protected void checkNextToken(LintScanner parser) {
        // Accepted token type: remove it from stack
        Token t = parser.getToken();
        if (parser.peekToken() instanceof ValueToken) {
            parser.getToken();
            Token t3 = parser.peekToken();
            if (t3 instanceof ScalarToken) {
                String strVal = ((ScalarToken)t3).getValue();
                try {
                    int val = Integer.parseInt(strVal);
                    if (val < minValue || val > maxValue) {
                        // Report new error
                        addViolation("Value out of range found. Range: min=" + minValue + " max=" + maxValue, t);
                    }
                }
                catch(NumberFormatException e) {
                    addViolation("Parse error: Non-integer value found for int-value-range-check", t);
                }
            }
        }
    }
}
