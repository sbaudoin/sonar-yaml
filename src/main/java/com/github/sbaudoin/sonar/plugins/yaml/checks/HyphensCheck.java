package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "HyphensCheck")
public class HyphensCheck extends YamlLintCheck {
    @RuleProperty(key = "max-spaces-after", description = "Maximal number of spaces allowed after hyphens", defaultValue = "1")
    int maxSpacesAfter;
}
