package org.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "NewLinesCheck")
public class NewLinesCheck extends YamlLintCheck {
    @RuleProperty(key = "type", description = "UNIX-typed ('unix') or DOS-typed ('dos') new line characters", defaultValue = "unix")
    String type;
}
