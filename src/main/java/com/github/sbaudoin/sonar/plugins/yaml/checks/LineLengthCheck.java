package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "LineLengthCheck")
public class LineLengthCheck extends YamlLintCheck {
    @RuleProperty(key = "max", description = "Maximal (inclusive) length of lines", defaultValue = "80")
    int max;

    @RuleProperty(key = "allow-non-breakable-words", description = "Allows or not non breakable words (without spaces inside) to overflow the limit", defaultValue = "true")
    boolean allowNonBreakableWords;

    @RuleProperty(key = "allow-non-breakable-inline-mappings", description = "Implies allow-non-breakable-words and extends it to also allow non-breakable words in inline mappings", defaultValue = "false")
    boolean allowNonBreakableInlineMappings;
}
