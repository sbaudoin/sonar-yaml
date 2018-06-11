package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "OctalValuesCheck")
public class OctalValuesCheck extends YamlLintCheck {
    @RuleProperty(key = "forbid-implicit-octal", description = "Tells if implicit octal values are forbidden or not", defaultValue = "false")
    boolean forbidImplicitOctal;

    @RuleProperty(key = "forbid-explicit-octal", description = "Tells if implicit octal values are forbidden or not", defaultValue = "false")
    boolean forbidExplicitOctal;
}
