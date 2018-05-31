package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.yaml.Utils;

import java.io.IOException;

public class YamlCheckTest extends TestCase {
    public void testSetGetRuleKey() {
        YamlCheck check = new DummyYamlCheck();
        RuleKey rk = RuleKey.of(CheckRepository.REPOSITORY_KEY, "foo");
        check.setRuleKey(rk);
        assertEquals(rk, check.getRuleKey());
    }

    public void testSetGetYamlSourceCode() throws IOException {
        YamlCheck check = new DummyYamlCheck();
        InputFile inputFile = Utils.getInputFile("src\\test\\files\\braces\\min-spaces-01.yaml");
        YamlSourceCode sc = new YamlSourceCode(inputFile);
        check.setYamlSourceCode(sc);

        assertEquals(sc, check.getYamlSourceCode());
    }


    private class DummyYamlCheck extends YamlCheck {
        @Override
        public void validate() {
            return;
        }
    }
}
