package org.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "CommentsCheck")
public class CommentsCheck extends YamlLintCheck {
    @RuleProperty(key = "require-starting-space", description = "Tells if a space character is required right after the # or not", defaultValue = "true")
    boolean requireStartingSpace;

    @RuleProperty(key = "min-spaces-from-content", description = "Minimal required number of spaces between a comment and its preceding content for inline comments", defaultValue = "2")
    int minSpacesFromContent;
}
