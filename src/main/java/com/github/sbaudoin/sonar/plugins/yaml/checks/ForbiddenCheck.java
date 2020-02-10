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

import com.github.sbaudoin.yamllint.LintScanner;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;

import java.io.IOException;

/**
 * Abstract class used to implement to forbidden key/scalar value checks
 */
public abstract class ForbiddenCheck extends YamlCheck {
    private static final Logger LOGGER = Loggers.get(ForbiddenCheck.class);


    @RuleProperty(key = "key-name", description = "Regexp that matches the forbidden name")
    String keyName;


    @Override
    public void validate() {
        if (yamlSourceCode == null) {
            throw new IllegalStateException("Source code not set, cannot validate anything");
        }

        try {
            LintScanner parser = new LintScanner(new StreamReader(yamlSourceCode.getContent()));
            if (!yamlSourceCode.hasCorrectSyntax()) {
                LOGGER.warn("Syntax error found, cannot continue checking keys: " + yamlSourceCode.getSyntaxError().getMessage());
                return;
            }
            while (parser.hasMoreTokens()) {
                Token t1 = parser.getToken();
                if (t1 instanceof KeyToken && parser.hasMoreTokens()) {
                    // Peek token (instead of get) in order to leave it in the stack so that it processed again when looping
                    Token t2 = parser.peekToken();
                    if (t2 instanceof ScalarToken && ((ScalarToken) t2).getValue().matches(keyName)) {
                        checkNextToken(parser);
                    }
                }
            }
        } catch (IOException e) {
            // Should not happen: a first call to getYamlSourceCode().getContent() was done in the constructor of
            // the YamlSourceCode instance of this check, but in case...
            LOGGER.warn("Cannot read source code", e);
        }
    }


    /**
     * Callback method used to implement a specific behavior when a key matching the {@code key-name} regex is found.
     * Implementations should carefully use the {@code peekToken()} and {@code getToken()} methods to make sure relevant,
     * unmatched tokens still remain in the stack of the scanner.
     *
     * @param parser the scanner/parser currently used to parse the YAML source file. The parser currently points to a
     *               key token that matches the {@code key-name} regex.
     */
    protected abstract void checkNextToken(LintScanner parser);

    /**
     * Adds a violation to the analyzed Yaml source for the passed token
     *
     * @param message the message that describes the violation
     * @param t the token for which a violation is to be added
     */
    protected void addViolation(String message, Token t) {
        getYamlSourceCode().addViolation(new YamlIssue(
                getRuleKey(),
                message,
                t.getStartMark().getLine() + 1,
                t.getStartMark().getColumn() + 1));
    }
}
