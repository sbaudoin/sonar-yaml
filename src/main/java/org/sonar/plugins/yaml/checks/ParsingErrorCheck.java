package org.sonar.plugins.yaml.checks;

import org.sonar.check.Rule;

@Rule(key = "ParsingErrorCheck")
public class ParsingErrorCheck extends YamlLintCheck {
    @Override
    public void validate() {
        // Do nothing, syntax errors are actually done in YamlSensor
        return;
    }
}
