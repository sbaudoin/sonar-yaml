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

import com.github.sbaudoin.yamllint.LintScanner;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.snakeyaml.tokens.ValueToken;
import org.springframework.boot.convert.DurationStyle;

import java.time.Duration;

/**
 * Check to be used that the YAML file does not contain duration values out of the specified range
 */
@Rule(key = "DurationInRangeCheck")
public class DurationInRangeCheck extends ForbiddenCheck {
    @RuleProperty(key = "minMillisValue", description = "Minimum value in milliseconds")
    int minMillis;

    @RuleProperty(key = "maxMillisValue", description = "Maximum value in milliseconds")
    int maxMillis;

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
                    Duration dur = DurationStyle.SIMPLE.parse(strVal);
                    if (dur.toMillis() < minMillis || dur.toMillis() > maxMillis) {
                        // Report new error
                        addViolation(getMessage(dur), t);
                    }
                }
                catch(IllegalArgumentException | IllegalStateException e) {
                    addViolation("Parse error: Non-duration found for duration-in-range-check", t);
                }
            }
        }
    }

    String getMessage(Duration dur) {
        return "Duration out of range found, in milliseconds="
                + dur.toMillis() + ", Range: minMillis=" + minMillis + " maxMillis=" + maxMillis;
    }
}
