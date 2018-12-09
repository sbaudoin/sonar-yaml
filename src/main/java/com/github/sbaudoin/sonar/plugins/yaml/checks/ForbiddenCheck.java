package com.github.sbaudoin.sonar.plugins.yaml.checks;

import com.github.sbaudoin.yamllint.LintScanner;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.yaml.snakeyaml.reader.StreamReader;
import org.yaml.snakeyaml.tokens.Token;

import java.io.IOException;

public abstract class ForbiddenCheck extends YamlCheck {
    private static final Logger LOGGER = Loggers.get(ForbiddenCheck.class);


    @Override
    public void validate() {
        if (yamlSourceCode == null) {
            throw new IllegalStateException("Source code not set, cannot validate anything");
        }

        try {
            LintScanner parser = new LintScanner(new StreamReader(yamlSourceCode.getContent()));
            if (!yamlSourceCode.hasCorrectSyntax()) {
                LOGGER.warn("Syntax error found, cannot continue checking keys: " + yamlSourceCode.getSyntaxError().getMessage());
                return;
            }
            while (parser.hasMoreTokens()) {
                checkNextToken(parser);
            }
        } catch (IOException e) {
            // Should not happen: a first call to getYamlSourceCode().getContent() was done in the constructor of
            // the YamlSourceCode instance of this check, but in case...
            LOGGER.warn("Cannot read source code", e);
        }
    }


    protected abstract void checkNextToken(LintScanner parser);

    protected void addViolation(String message, Token t) {
        getYamlSourceCode().addViolation(new YamlIssue(
                getRuleKey(),
                message,
                t.getStartMark().getLine() + 1,
                t.getStartMark().getColumn() + 1));
    }
}
