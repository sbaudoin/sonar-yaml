package org.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "CommasCheck")
public class CommasCheck extends YamlLintCheck {
    @RuleProperty(key = "max-spaces-before", description = "Maximal number of spaces allowed before commas (use -1 to disable)", defaultValue = "0")
    int maxSpacesBefore;

    @RuleProperty(key = "min-spaces-after", description = "Minimal number of spaces after commas", defaultValue = "1")
    int minSpacesAfter;

    @RuleProperty(key = "max-spaces-after", description = "Maximal number of spaces allowed after commas (use -1 to disable)", defaultValue = "1")
    int maxSpacesAfter;
}
