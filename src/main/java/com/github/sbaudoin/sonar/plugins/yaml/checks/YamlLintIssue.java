package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.api.rule.RuleKey;
import com.github.sbaudoin.yamllint.LintProblem;

/**
 * Wrapper class for {@see LintProblem} to add some additional features
 */
public class YamlLintIssue extends YamlIssue {
    /**
     * Constructor for "standard" (non-syntactical issue)
     *
     * @param problem the source {@link LintProblem}
     * @param ruleKey the key of the rule that identified the issue
     */
    public YamlLintIssue(LintProblem problem, RuleKey ruleKey) {
        this(problem, ruleKey, false);
    }

    /**
     * Constructor
     *
     * @param problem the source {@link LintProblem}
     * @param ruleKey the key of the rule that identified the issue
     * @param syntaxError {@code true} if this issue corresponds to a syntax error, {@code false} if not
     */
    public YamlLintIssue(LintProblem problem, RuleKey ruleKey, boolean syntaxError) {
        super(ruleKey, problem.getMessage(), problem.getLine(), problem.getColumn(), syntaxError);
    }
}
