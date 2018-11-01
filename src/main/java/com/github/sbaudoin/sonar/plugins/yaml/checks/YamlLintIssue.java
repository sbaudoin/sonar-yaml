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

import org.sonar.api.rule.RuleKey;
import com.github.sbaudoin.yamllint.LintProblem;

/**
 * Wrapper class for {@link com.github.sbaudoin.yamllint.LintProblem} to add some additional features
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
