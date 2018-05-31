package org.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "EmptyLinesCheck")
public class EmptyLinesCheck extends YamlLintCheck {
    @RuleProperty(key = "max", description = "Maximal number of consecutive empty lines allowed in the document", defaultValue = "2")
    int max;

    @RuleProperty(key = "max-start", description = "Maximal number of empty lines allowed at the beginning of the file. This option takes precedence over max.", defaultValue = "0")
    int maxStart;

    @RuleProperty(key = "max-end", description = "Maximal number of empty lines allowed at the end of the file. This option takes precedence over max.", defaultValue = "0")
    int maxEnd;
}
