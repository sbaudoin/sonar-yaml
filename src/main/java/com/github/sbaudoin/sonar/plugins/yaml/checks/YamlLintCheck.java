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

import org.sonar.api.utils.WildcardPattern;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.RuleProperty;
import com.github.sbaudoin.yamllint.LintProblem;
import com.github.sbaudoin.yamllint.Linter;
import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public abstract class YamlLintCheck extends YamlCheck {
    private static final Logger LOGGER = Loggers.get(YamlLintCheck.class);


    @Override
    public void validate() {
        if (yamlSourceCode == null) {
            throw new IllegalStateException("Source code not set, cannot validate anything");
        }

        try {
            List<LintProblem> problems = Linter.getCosmeticProblems(getYamlSourceCode().getContent(), getYamlLintconfig(), null);
            LOGGER.debug("Problems found: " + problems);
            for (LintProblem problem : problems) {
                LOGGER.debug("Creating violation for " + problem);
                createViolation(problem);
            }
        } catch (YamlLintConfigException e) {
            LOGGER.warn("Cannot get YamlLintConfig for rule '" + getLintRuleId() + "'", e);

        } catch (IOException e) {
            // Should not happen: a first call to getYamlSourceCode().getContent() was done in the constructor of
            // the YamlSourceCode instance of this check, but in case...
            LOGGER.warn("Cannot read source code", e);
        }
    }


    /**
     * Registers a violation that is no a syntax error
     *
     * @param violation a problem representing the violation
     */
    protected final void createViolation(LintProblem violation) {
        getYamlSourceCode().addViolation(new YamlLintIssue(violation, getRuleKey()));
    }

    /**
     * Registers a violation that may be said to be a syntax error
     *
     * @param violation a problem representing the violation
     * @param syntaxError {@code true} if the violation must be declared as a syntax error, {@code false} if not (this
     *                    is an "ordinary" violation)
     */
    protected final void createViolation(LintProblem violation, boolean syntaxError) {
        getYamlSourceCode().addViolation(new YamlLintIssue(violation, getRuleKey(), syntaxError));
    }

    /**
     * Check with ant style filepattern if the file is included.
     */
    protected boolean isFileIncluded(@Nullable String filePattern) {
        if (filePattern != null) {
            return WildcardPattern.create(filePattern)
                    .match(getYamlSourceCode().getYamlFile().filename());

        } else {
            return true;
        }
    }

    /**
     * Returns the YAML Lint ID of the rule corresponding to this check. The rule ID is calculated from the class name as follows:
     * <ul>
     *     <li>The suffix "Check" is removed from the class name</li>
     *     <li>An hyphen ("-") is inserted before every capital letter of the class name (except for the first letter)</li>
     *     <li>All lowercase</li>
     * </ul>
     * <p>Example: if the class name is {@code FooBarCheck} then the YAML Lint ID returned by this method will be {@code "foo-bar"}</p>
     *
     * @return a string that is a YAML Lint rule ID
     */
    protected String getLintRuleId() {
        return this.getClass().getName().replaceAll(".*\\.", "").replaceAll("Check$", "").replaceAll("([A-Z])", "-$1").substring(1).toLowerCase();
    }

    /**
     * Returns an instance of {@code YamlLintConfig} that corresponds to the configuration of the current rule
     *
     * @return an instance of {@code YamlLintConfig}
     * @throws YamlLintConfigException if an error occurred building the instance of {@code YamlLintConfig}
     * @see YamlLintConfig
     */
    protected YamlLintConfig getYamlLintconfig() throws YamlLintConfigException {
        StringBuilder propsSB = new StringBuilder();
        for (Field f : getClass().getDeclaredFields()) {
            RuleProperty rp = f.getAnnotation(RuleProperty.class);
            LOGGER.debug("Got RuleProperty " + rp);
            if (rp != null) {
                try {
                    propsSB.append("    ").append(rp.key()).append(": ").append(f.get(this)).append("\n");
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Cannot get field value for '" + f.getName() + "'", e);
                    return null;
                }
            }
        }

        StringBuilder confSB = new StringBuilder("---\n").append("rules:\n").append("  ").append(getLintRuleId()).append(":");
        if (propsSB.length() == 0) {
            confSB.append(" enable");
        } else {
            confSB.append("\n").append(propsSB);
        }

        LOGGER.debug("YAMLLint config for rule " + getRuleKey() + "/" + getLintRuleId() + ": '" + confSB.toString() + "'");
        return new YamlLintConfig(confSB.toString());
    }
}
