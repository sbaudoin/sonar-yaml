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

import org.sonar.api.rule.RuleKey;

/**
 * Wrapper class for {@link com.github.sbaudoin.yamllint.LintProblem} to add some additional features
 */
public class YamlIssue {
    /**
     * The {@code RuleKey} of the rule that identified the issue
     */
    protected final RuleKey ruleKey;

    /**
     * Flag that tells if this issue represents a syntax error or not
     */
    protected boolean syntaxError;

    /**
     * The line number at which the issue was found
     */
    protected int line;

    /**
     * The column at which the issue was found
     */
    protected int column;

    /**
     * A message describing the issue
     */
    protected String message;


    /**
     * Constructor for "standard" (non-syntactical issue)
     *
     * @param ruleKey the key of the rule that identified the issue
     * @param message a message describing the issue
     * @param line the line number at which the issue was found
     * @param column the column number at which the issue was found
     */
    public YamlIssue(RuleKey ruleKey, String message, int line, int column) {
        this.ruleKey = ruleKey;
        this.syntaxError = false;
        this.message = message;
        this.line = line;
        this.column = column;
    }

    /**
     * Constructor for "standard" (non-syntactical issue)
     *
     * @param ruleKey the key of the rule that identified the issue
     * @param line the line number at which the issue was found
     * @param column the column number at which the issue was found
     * @param message a message describing the issue
     * @param syntaxError {@code true} if this issue corresponds to a syntax error, {@code false} otherwise
     */
    public YamlIssue(RuleKey ruleKey, String message, int line, int column, boolean syntaxError) {
        this.ruleKey = ruleKey;
        this.message = message;
        this.line = line;
        this.column = column;
        this.syntaxError = syntaxError;
    }

    /**
     * Returns a message describing the issue
     *
     * @return a description of the issue
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the line number at which the issue was found
     *
     * @return the line number where the issue was found
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column number at which the issue was found
     *
     * @return the column number where the issue was found
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the key of the rule associated with this issue
     *
     * @return the key of the rule associated with this issue
     */
    public RuleKey getRuleKey() {
        return ruleKey;
    }

    /**
     * Tells if this issue corresponds to a syntax error
     *
     * @return {@code true} if this issue relates to a syntax error, {@code false} otherwise
     */
    public boolean isSyntaxError() {
        return syntaxError;
    }
}
