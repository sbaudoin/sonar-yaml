package org.sonar.plugins.yaml.checks;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.scanner.ScannerException;
import org.yaml.snakeyaml.tokens.KeyToken;
import org.yaml.snakeyaml.tokens.ScalarToken;
import org.yaml.snakeyaml.tokens.Token;
import org.yaml.yamllint.LintScanner;

import java.io.IOException;

/**
 * Check to be used that the YAML file does not contain forbidden keys
 */
@Rule(key = "ForbiddenKeyCheck")
public class ForbiddenKeyCheck extends YamlCheck {
    private static final Logger LOGGER = Loggers.get(ForbiddenKeyCheck.class);


    @RuleProperty(key = "key-name", description = "Regexp that matches the forbidden name")
    String keyName;


    @Override
    public void validate() {
        if (yamlSourceCode == null) {
            throw new IllegalStateException("Source code not set, cannot validate anything");
        }

        try {
            LintScanner parser = new LintScanner(new StreamReader(yamlSourceCode.getContent()));
            while (parser.hasMoreTokens()) {
                Token t1 = parser.getToken();
                if (t1 instanceof KeyToken && parser.hasMoreTokens()) {
                    Token t2 = parser.getToken();
                    if (t2 instanceof ScalarToken && ((ScalarToken)t2).getValue().matches(keyName)) {
                        // Report new error
                        getYamlSourceCode().addViolation(new YamlIssue(
                                getRuleKey(),
                                "Forbidden key found",
                                t2.getStartMark().getLine() + 1,
                                t2.getStartMark().getColumn() + 1));
                    }
                }
            }
        } catch (ScannerException e) {
            LOGGER.warn("Syntax error found, cannot continue checking keys: " + e.getMessage());
            LOGGER.debug("Corresponding stacktrace:", e);
        } catch (IOException e) {
            // Should not happen: a first call to getYamlSourceCode().getContent() was done in the constructor of
            // the YamlSourceCode instance of this check, but in case...
            LOGGER.warn("Cannot read source code", e);
        }
    }
}
