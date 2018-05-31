package org.sonar.plugins.yaml.checks;

import org.sonar.api.rule.RuleKey;
import org.yaml.yamllint.LintProblem;

/**
 * Wrapper class for {@see LintProblem} to add some additional features
 */
public class YamlIssue {
    protected final RuleKey ruleKey;
    protected boolean syntaxError;
    protected int line;
    protected int column;
    protected String message;


    /**
     * Constructor for "standard" (non-syntactical issue)
     *
     * @param ruleKey the key of the rule that identified the issue
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
     */
    public YamlIssue(RuleKey ruleKey, String message, int line, int column, boolean syntaxError) {
        this.ruleKey = ruleKey;
        this.message = message;
        this.line = line;
        this.column = column;
        this.syntaxError = syntaxError;
    }

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
