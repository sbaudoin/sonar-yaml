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

import com.github.sbaudoin.yamllint.YamlLintConfig;
import org.sonar.api.rule.RuleKey;

/**
 * Abstract class that all YAML checks should extend
 */
public abstract class YamlCheck {
    protected YamlLintConfig config = null;


    /**
     * The {@code RuleKey} of this check
     */
    protected RuleKey ruleKey = null;

    /**
     * The {@code YamlSourceCode} instance this check has analyzed or will analyze
     */
    protected YamlSourceCode yamlSourceCode = null;


    /**
     * Sets the {@code RuleKey} of this check
     *
     * @param ruleKey the {@code RuleKey} of this check
     */
    public final void setRuleKey(RuleKey ruleKey) {
        this.ruleKey = ruleKey;
    }

    /**
     * Returns the {@code RuleKey} of this check
     *
     * @return the {@code RuleKey} of this check, possibly {@code null}
     */
    public RuleKey getRuleKey() {
        return ruleKey;
    }

    /**
     * Sets the source code on which the check is to be performed
     * <p><strong>Call this method before calling {@link #validate()}!</strong></p>
     *
     * @param yamlSourceCode the source code to be checked
     */
    public void setYamlSourceCode(YamlSourceCode yamlSourceCode) {
        this.yamlSourceCode = yamlSourceCode;
    }

    /**
     * Returns the {@code YamlSourceCode} instance this check has analyzed or will analyze
     *
     * @return the {@code YamlSourceCode} instance this check has analyzed or will analyze
     */
    public YamlSourceCode getYamlSourceCode() {
        return yamlSourceCode;
    }

    /**
     * Sets the configuration to use to validate this rule. If not set, the lint configuration is guessed from the rule
     * properties.
     *
     * @param config the lint configuration to use to validate this rule
     */
    public void setConfig(YamlLintConfig config) {
        this.config = config;
    }

    /**
     * Returns a (unique) ID for this check. By default, the rule ID is calculated from the class name as follows:
     * <ul>
     *     <li>The suffix "Check" is removed from the class name</li>
     *     <li>An hyphen ("-") is inserted before every capital letter of the class name (except for the first letter)</li>
     *     <li>All lowercase</li>
     * </ul>
     * <p>Example: if the class name is {@code FooBarCheck} then the YAML Lint ID returned by this method will be {@code "foo-bar"}</p>
     *
     * @return a string that uniquely identifies this rule
     */
    public String getId() {
        return this.getClass().getName().replaceAll("^.*\\.([^.])", "$1").replaceAll("Check$", "").replaceAll("([A-Z])", "-$1").substring(1).toLowerCase();
    }

    /**
     * Validates a source code, creating violations for each error found.
     * <p>The default implementation executes the YAMLLint rule whose name corresponds to the check class name
     * (minus the suffix {@literal "Check"}.</p>
     *
     * @throws IllegalStateException if there is no source code to validate, i.e. if {@link #setYamlSourceCode(YamlSourceCode)} has not been called first
     */
    public abstract void validate();
}
