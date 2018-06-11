package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "IndentationCheck")
public class IndentationCheck extends YamlLintCheck {
    @RuleProperty(key = "spaces", description = "The indentation width, in spaces", defaultValue = "consistent")
    String spaces;

    @RuleProperty(key = "indent-sequences", description = "Tells whether block sequences should be indented or not", defaultValue = "true")
    boolean indentSequences;

    @RuleProperty(key = "check-multi-line-strings", description = "Tells whether to lint indentation in multi-line strings", defaultValue = "false")
    boolean checkMultiLineStrings;
}
