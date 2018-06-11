package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "DocumentEndCheck")
public class DocumentEndCheck extends YamlLintCheck {
    @RuleProperty(key = "present", description = "Tells if the document end marker is required or is forbidden", defaultValue = "true")
    boolean present;
}
