package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "BracketsCheck")
public class BracketsCheck extends YamlLintCheck {
    @RuleProperty(key = "min-spaces-inside", description = "Minimal number of spaces required inside brackets", defaultValue = "0")
    int minSpacesInside;

    @RuleProperty(key = "max-spaces-inside", description = "Maximal number of spaces required inside brackets", defaultValue = "0")
    int maxSpacesInside;

    @RuleProperty(key = "min-spaces-inside-empty", description = "Minimal number of spaces required inside empty brackets", defaultValue = "-1")
    int minSpacesInsideEmpty;

    @RuleProperty(key = "max-spaces-inside-empty", description = "Maximal number of spaces required inside empty brackets", defaultValue = "-1")
    int maxSpacesInsideEmpty;
}
