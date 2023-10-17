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
import java.util.stream.Collectors;

/**
 * Abstract class for all YAML checks representing a YAML lint rule
 */
public abstract class YamlLintCheck extends YamlCheck {
    private static final Logger LOGGER = Loggers.get(YamlLintCheck.class);


    @Override
    public void validate() {
        if (yamlSourceCode == null) {
            throw new IllegalStateException("Source code not set, cannot validate anything");
        }

        try {
            List<LintProblem> allProblems = Linter.getCosmeticProblems(getYamlSourceCode().getContent(), getYamlLintconfig(), null);
            // Filter out problems other than those coming from the current rule
            List<LintProblem> problems = allProblems.stream().filter(p -> p.getRuleId().equals(getLintRuleId())).collect(Collectors.toList());
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
     * Tells with Ant style {@code filepattern} if the file analyzed being is included. / are always used as the
     * file separator
     *
     * @param filePattern an Ant style file pattern ({@code **\/*.yaml}, etc.)
     * @return {@code true} is the path of the file source code being checked matches the passed pattern,
     * {@code false} otherwise
     */
    protected boolean isFileIncluded(@Nullable String filePattern) {
        if (filePattern != null) {
            return WildcardPattern.create(filePattern)
                    .match(getYamlSourceCode().getYamlFile().uri().getPath());

        } else {
            return true;
        }
    }

    /**
     * Returns the YAML Lint ID of the rule corresponding to this check. This is the same as {@link YamlCheck#getId()} }.
     *
     * @return a string that is a YAML Lint rule ID
     * @see YamlCheck#getId()
     */
    protected String getLintRuleId() {
        return getId();
    }

    /**
     * Returns an instance of {@code YamlLintConfig} that corresponds to the configuration of the current rule
     *
     * @return an instance of {@code YamlLintConfig}
     * @throws YamlLintConfigException if an error occurred building the instance of {@code YamlLintConfig}
     * @see YamlLintConfig
     */
    protected YamlLintConfig getYamlLintconfig() throws YamlLintConfigException {
        if (config != null) {
            return config;
        }

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

        return getYamlLintconfig(propsSB);
    }

    /**
     * Returns an instance of {@code YamlLintConfig} that corresponds to the passed YAML configuration for the current rule
     *
     * @param conf the rule configuration in the YAML syntax. The passed configuration must be indented (starting with 4
     *             spaces) and without the rule name. Example:
     *             <pre>
     *                 a_key: value1
     *                 another_key: value2
     *             </pre>
     *             Pass an empty string ({@code ""}) to simply enable the rule with the default configuration.
     * @return an instance of {@code YamlLintConfig}
     * @throws YamlLintConfigException if an error occurred building the instance of {@code YamlLintConfig}
     * @see YamlLintConfig
     */
    protected YamlLintConfig getYamlLintconfig(CharSequence conf) throws YamlLintConfigException {
        StringBuilder confSB = new StringBuilder("---\n").append("rules:\n").append("  ").append(getLintRuleId()).append(":");
        if (conf.length() == 0) {
            confSB.append(" enable");
        } else {
            confSB.append("\n").append(conf);
        }

        LOGGER.debug("YAMLLint config for rule " + getRuleKey() + "/" + getLintRuleId() + ": '" + confSB + "'");
        return new YamlLintConfig(confSB.toString());
    }
}
