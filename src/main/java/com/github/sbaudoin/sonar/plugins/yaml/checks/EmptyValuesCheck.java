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

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

/**
 * Check for empty values
 */
@Rule(key = "EmptyValuesCheck")
public class EmptyValuesCheck extends YamlLintCheck {
    @RuleProperty(key = "forbid-in-block-mappings", description = "Prevent or not empty values in block mappings", defaultValue = "true")
    boolean forbidInBlockMappings;

    @RuleProperty(key = "forbid-in-flow-mappings", description = "Prevent or not empty values in flow mappings", defaultValue = "true")
    boolean forbidInFlowMappings;

    @RuleProperty(key = "forbid-in-block-sequences", description = "Prevent empty values in block sequences", defaultValue = "true")
    boolean forbidInBlockSequences;
}
