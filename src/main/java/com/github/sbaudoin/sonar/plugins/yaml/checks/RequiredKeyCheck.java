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
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.snakeyaml.tokens.ValueToken;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

/**
 * Class used to implement the required key/scalar value checks
 */
@Rule(key = "RequiredKeyCheck")
public class RequiredKeyCheck extends YamlCheck {
    private static final Logger LOGGER = Loggers.get(RequiredKeyCheck.class);
    private static final int FIRST_COLUMN = 0;

    private int issueLine = 0;

    @RuleProperty(key = "parent-key-name", description = "Regexp that matches the prerequisite parent-key-name")
    String keyName;

    @RuleProperty(key = "parent-key-value", description = "Regexp that matches the value for the prerequisite parent-key-name")
    String keyValue;

    @RuleProperty(key = "parent-key-name-root", description = "Filter only root keys for the parent-key-name (allowed values: 'yes', 'not' or 'anywhere')", defaultValue = "anywhere")
    String isKeyNameAtRoot;

    @RuleProperty(key = "required-key-name", description = "Regexp that matches the required key name for the required-key-name")
    String requiredKeyName;

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
            boolean isKeyPresent = false;
            boolean isRequiredKeyPresent = false;
            while (parser.hasMoreTokens()) {
                Token t1 = parser.getToken();
                if (t1 instanceof KeyToken && parser.hasMoreTokens()) {
                  // Peek token (instead of get) in order to leave it in the stack so that it processed again when looping
                  Token t2 = parser.peekToken();
                  if (t2 instanceof ScalarToken) {
                      if (((ScalarToken)t2).getValue().matches(keyName)) {

                          boolean isNewMatch = checkValue(parser);

                          int column = t1.getStartMark().getColumn();
                          if ((isKeyNameAtRoot.equalsIgnoreCase("yes") && column != 0) ||
                                  (isKeyNameAtRoot.equalsIgnoreCase("not") && column == 0)) {
                            continue;
                          }
                          
                          if (isKeyPresent && isNewMatch && !isRequiredKeyPresent) {
                              checkNextToken();
                          }
                          isKeyPresent = isNewMatch;
                          isRequiredKeyPresent = (!isNewMatch) && isRequiredKeyPresent;
                          issueLine = (isNewMatch) ? t2.getStartMark().getLine() : issueLine;

                      } else if (((ScalarToken) t2).getValue().matches(requiredKeyName) && isKeyPresent) {
                          isRequiredKeyPresent = true;
                      }     
                  }
                }
            }
            if (!isRequiredKeyPresent && isKeyPresent) {
                checkNextToken();
            }
        } catch (IOException e) {
            // Should not happen: a first call to getYamlSourceCode().getContent() was done in the constructor of
            // the YamlSourceCode instance of this check, but in case...
            LOGGER.warn("Cannot read source code", e);
        }
    }


    private boolean checkValue(LintScanner parser) {
        boolean isKeyPresent = false;
        parser.getToken();
        if (parser.peekToken() instanceof ValueToken) {
            parser.getToken();
            Token t3 = parser.peekToken();
            if (t3 instanceof ScalarToken) {
                Matcher m = Pattern.compile("(?m)" + keyValue).matcher(((ScalarToken)t3).getValue());
                if (m.find()) {
                    isKeyPresent = true;
                }
            }
        }
        return isKeyPresent;
    }


    /**
     * Callback method used to implement a specific behavior when a key matching the {@code key-name} regex is NOT found.
     * Implementations should carefully use the {@code peekToken()} and {@code getToken()} methods to make sure relevant,
     * unmatched tokens still remain in the stack of the scanner.
     */
    private void checkNextToken() {
        // Just report new error
        addViolation("Required " + requiredKeyName + " key not found");
    }

    /**
     * Adds a violation to the analyzed Yaml source
     *
     * @param message the message that describes the violation
     */
    private void addViolation(String message) {
        getYamlSourceCode()
            .addViolation(new YamlIssue(
                getRuleKey(),
                message,
                issueLine + 1,
                FIRST_COLUMN + 1
            )
        );
    }
}
