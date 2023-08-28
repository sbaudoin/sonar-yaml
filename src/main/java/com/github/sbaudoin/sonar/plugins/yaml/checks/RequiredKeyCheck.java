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
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.tokens.BlockEndToken;
import org.yaml.snakeyaml.tokens.BlockMappingStartToken;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.snakeyaml.tokens.ValueToken;

import java.util.Stack;
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

    @RuleProperty(key = "parent-key-name", description = "Regexp that matches the required parent-key-name")
    String parentKeyName;

    @RuleProperty(key = "parent-key-value", description = "Regexp that matches the value for the required parent-key")
    String parentKeyValue;

    @RuleProperty(key = "parent-key-at-root", description = "Require the parent-key to be at root level (allowed values: 'yes', 'not' or 'anywhere')", defaultValue = "anywhere")
    String isParentKeyAtRoot;

    @RuleProperty(key = "included-ancestors", description = "Regexp that matches the key's ancestors to include, for example '<root>:nesting1.*'")
    String includedAncestors;

    @RuleProperty(key = "excluded-ancestors", description = "Regexp that matches the key's ancestors to exclude, for example '.*:nesting2:nesting3'")
    String excludedAncestors;

    @RuleProperty(key = "required-key-name", description = "Regexp that matches the name of the required key")
    String requiredKeyName;
    private Pattern reqKeyNamePattern;

    protected void initializePatterns() {
        reqKeyNamePattern = Pattern.compile(requiredKeyName);
    }

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
            initializePatterns();
            final boolean parentCheck = parentKeyName != null && !parentKeyName.isEmpty();
            final boolean ancestorsCheck = (includedAncestors != null && !includedAncestors.isEmpty()) || (excludedAncestors != null && !excludedAncestors.isEmpty());
            final Pattern parentKeyNamePattern = parentCheck ? Pattern.compile(parentKeyName) : null;
            final Pattern parentValuePattern = parentCheck ? Pattern.compile("(?m)" + parentKeyValue) : null;
            final Pattern inclAncestorsPattern = includedAncestors != null && !includedAncestors.isEmpty() ? Pattern.compile(includedAncestors) : null;
            final Pattern exclAncestorsPattern = excludedAncestors != null && !excludedAncestors.isEmpty() ? Pattern.compile(excludedAncestors) : null;

            final Stack<String> ancestors = new Stack<>();
            String prevKeyScalarValue = "<root>";
            boolean prevAncestorsMatch = false;
            int ancestorLine = 0;

            boolean parentMatch = false;
            boolean isRequiredKeyPresent = false;

            while (parser.hasMoreTokens()) {
                Token t1 = parser.getToken();
                if (ancestorsCheck) {
                    if (t1 instanceof BlockMappingStartToken) {
                        ancestors.push(prevKeyScalarValue);
                        ancestorLine = t1.getStartMark().getLine() - 1; // one line up
                    } else if (t1 instanceof BlockEndToken) {
                        if (!ancestors.isEmpty()) ancestors.pop();
                    }
                }
                if (t1 instanceof KeyToken && parser.hasMoreTokens()) {
                  // Peek token (instead of get) in order to leave it in the stack so that it processed again when looping
                  Token t2 = parser.peekToken();
                  if (t2 instanceof ScalarToken) {
                      String keyScalarValue = ((ScalarToken) t2).getValue();
                      boolean ancestorsMatch = ancestorsCheck && ancestorsMatch(ancestors, inclAncestorsPattern, exclAncestorsPattern);
                      boolean newAncestorsMatch = !prevAncestorsMatch && ancestorsMatch;
                      boolean justLostAncestorsMatch = prevAncestorsMatch && !ancestorsMatch;
                      if (parentCheck && parentKeyMatches(keyScalarValue, parentKeyNamePattern)) {
                              boolean newParentMatch = parentValueMatches(parser, parentValuePattern);
                              int column = t1.getStartMark().getColumn();
                              if ((isParentKeyAtRoot.equalsIgnoreCase("yes") && column != 0) ||
                                      (isParentKeyAtRoot.equalsIgnoreCase("not") && column == 0)) {
                                  continue;
                              }

                              if (parentMatch && newParentMatch && !isRequiredKeyPresent) {
                                  checkNextToken();// violation
                              }
                              parentMatch = newParentMatch;
                              isRequiredKeyPresent = (!newParentMatch) && isRequiredKeyPresent;
                              issueLine = (newParentMatch) ? t2.getStartMark().getLine() : issueLine;
                      } else {
                          boolean reqKeyMatches = reqKeyNamePattern.matcher(keyScalarValue).matches();
                          if (reqKeyMatches) {
                              if (parentCheck && ancestorsCheck) {
                                  isRequiredKeyPresent = parentMatch && ancestorsMatch;
                              }
                              else if (parentCheck) {
                                  isRequiredKeyPresent = parentMatch;
                              }
                              else if (ancestorsCheck) {
                                  isRequiredKeyPresent = ancestorsMatch;
                              }
                          }
                          issueLine = newAncestorsMatch ? ancestorLine : issueLine;
                          if (justLostAncestorsMatch && (!parentCheck || parentMatch)) {
                              if (!isRequiredKeyPresent) {
                                  checkNextToken(); // violation
                              }
                              else {
                                  isRequiredKeyPresent = false; // start over
                              }
                          }
                      }
                      prevKeyScalarValue = keyScalarValue;
                      prevAncestorsMatch = ancestorsMatch;
                  }
                }
            }
            if (!isRequiredKeyPresent && ((!parentCheck || parentMatch) && (!ancestorsCheck || prevAncestorsMatch))) {
                checkNextToken();// violation
            }
        } catch (IOException e) {
            // Should not happen: a first call to getYamlSourceCode().getContent() was done in the constructor of
            // the YamlSourceCode instance of this check, but in case...
            LOGGER.warn("Cannot read source code", e);
        }
    }

    private boolean parentKeyMatches(String keyScalarValue, Pattern parentKeyNamePattern) {
        return parentKeyNamePattern != null ? parentKeyNamePattern.matcher(keyScalarValue).matches() : true;
    }

    private boolean ancestorsMatch(Stack<String> ancestors, Pattern inclAncestorsPattern, Pattern exclAncestorsPattern) {
        String ancestorsString = String.join(":", ancestors);
        boolean match = inclAncestorsPattern != null ? inclAncestorsPattern.matcher(ancestorsString).matches() : true;
        match = match && (exclAncestorsPattern != null ? !exclAncestorsPattern.matcher(ancestorsString).matches() : true);
        return match;
    }

    private boolean parentValueMatches(LintScanner parser, Pattern parentValuePattern) {
        boolean isMatchingValue = false;
        parser.getToken();
        if (parser.peekToken() instanceof ValueToken) {
            parser.getToken();
            Token t3 = parser.peekToken();
            if (t3 instanceof ScalarToken) {
                Matcher m = parentValuePattern.matcher(((ScalarToken)t3).getValue());
                if (m.find()) {
                    isMatchingValue = true;
                }
            }
        }
        return isMatchingValue;
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
