package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "EmptyValuesCheck")
public class EmptyValuesCheck extends YamlLintCheck {
    @RuleProperty(key = "forbid-in-block-mappings", description = "Prevents or not empty values in block mappings", defaultValue = "false")
    boolean forbidInBlockMappings;

    @RuleProperty(key = "forbid-in-flow-mappings", description = "Prevents or not empty values in flow mappings", defaultValue = "false")
    boolean forbidInFlowMappings;
}
