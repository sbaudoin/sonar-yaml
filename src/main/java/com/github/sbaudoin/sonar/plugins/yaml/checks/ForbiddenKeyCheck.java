/**
 * Copyright (c) 2018, Sylvain Baudoin
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

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import com.github.sbaudoin.yamllint.LintScanner;

import java.io.IOException;

/**
 * Check to be used that the YAML file does not contain forbidden keys
 */
@Rule(key = "ForbiddenKeyCheck")
public class ForbiddenKeyCheck extends ForbiddenCheck {
    private static final Logger LOGGER = Loggers.get(ForbiddenKeyCheck.class);


    @RuleProperty(key = "key-name", description = "Regexp that matches the forbidden name")
    String keyName;


    protected void checkNextToken(LintScanner parser) {
        Token t1 = parser.getToken();
        if (t1 instanceof KeyToken && parser.hasMoreTokens()) {
            // Peek token (instead of get) in order to leave it in the stack so that it processed again when looping
            Token t2 = parser.peekToken();
            if (t2 instanceof ScalarToken && ((ScalarToken)t2).getValue().matches(keyName)) {
                // Report new error
                addViolation("Forbidden key found", t2);
            }
        }
    }
}
