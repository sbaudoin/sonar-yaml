package org.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "ColonsCheck")
public class ColonsCheck extends YamlLintCheck {
    @RuleProperty(key = "max-spaces-before", description = "Maximal number of spaces allowed before colons (use -1 to disable)", defaultValue = "0")
    int maxSpacesBefore;

    @RuleProperty(key = "max-spaces-after", description = "Maximal number of spaces allowed after colons (use -1 to disable)", defaultValue = "1")
    int maxSpacesAfter;
}
