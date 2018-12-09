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

import com.github.sbaudoin.yamllint.LintScanner;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.snakeyaml.tokens.ValueToken;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Check to be used that the YAML file does not contain forbidden values
 */
@Rule(key = "ForbiddenValueCheck")
public class ForbiddenValueCheck extends YamlCheck {
    private static final Logger LOGGER = Loggers.get(ForbiddenValueCheck.class);


    @RuleProperty(key = "key-name", description = "Regexp that matches the name of the key whose value is forbidden")
    String keyName;

    @RuleProperty(key = "value", description = "Regexp that matches the forbidden value")
    String value;


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
                YamlIssue issue = analyzeNextToken(parser);
                if (issue != null) {
                    getYamlSourceCode().addViolation(issue);
                }
            }
        } catch (IOException e) {
            // Should not happen: a first call to getYamlSourceCode().getContent() was done in the constructor of
            // the YamlSourceCode instance of this check, but in case...
            LOGGER.warn("Cannot read source code", e);
        }
    }

    /**
     * Takes the next token and, if it is a key that matches the {@code key-name} regex, analyzes its value against the
     * {@code value} regex, possibly returning an issue if there is a match
     *
     * @param parser the scanner that holds the tokens
     * @return an issue if both the key name and value match
     */
    private YamlIssue analyzeNextToken(LintScanner parser) {
        Token t1 = parser.getToken();
        if (t1 instanceof KeyToken && parser.hasMoreTokens()) {
            Token t2 = parser.peekToken();
            if (t2 instanceof ScalarToken && ((ScalarToken)t2).getValue().matches(keyName)) {
                // Accepted token type: remove it from stack
                parser.getToken();
                if (parser.peekToken() instanceof ValueToken) {
                    parser.getToken();
                    Token t3 = parser.peekToken();
                    if (t3 instanceof ScalarToken) {
                        Matcher m = Pattern.compile("(?m)" + value).matcher(((ScalarToken)t3).getValue());
                        if (m.find()) {
                            // Report new error
                            return new YamlIssue(
                                    getRuleKey(),
                                    "Forbidden value found",
                                    t2.getStartMark().getLine() + 1,
                                    t2.getStartMark().getColumn() + 1);
                        }
                    }
                }
            }
        }

        // No match (key or value): return nothing
        return null;
    }
}
