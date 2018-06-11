package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "DocumentStartCheck")
public class DocumentStartCheck extends YamlLintCheck {
    @RuleProperty(key = "present", description = "Tells if the document start marker is required or is forbidden", defaultValue = "true")
    boolean present;
}
