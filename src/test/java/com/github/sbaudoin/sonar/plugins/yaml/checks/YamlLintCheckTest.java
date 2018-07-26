package com.github.sbaudoin.sonar.plugins.yaml.checks;

import com.github.sbaudoin.sonar.plugins.yaml.Utils;
import com.github.sbaudoin.yamllint.LintProblem;
import com.github.sbaudoin.yamllint.YamlLintConfigException;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class YamlLintCheckTest {
    @Rule
    public LogTester logTester = new LogTester();


    @Test
    public void testValidate() throws IOException {
        try {
            new DummyYamlCheck().validate();
            fail("validate should fail if no source code");
        } catch (IllegalStateException e) {
            assertEquals("Source code not set, cannot validate anything", e.getMessage());
        }

        getDummyCheck().validate();
        assertEquals("Cannot get YamlLintConfig for rule 'yaml-lint-check-test$-dummy-yaml'", logTester.logs(LoggerLevel.WARN).get(0));

        YamlSourceCode code = new YamlSourceCode(Utils.getInputFile("src" + File.separator + "test" + File.separator + "files" + File.separator + "dummy-file.yaml"));
        YamlSourceCode spy = spy(code);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));
        DummyYamlCheck check1 = new DummyYamlCheck();
        check1.setYamlSourceCode(spy);
        check1.validate();
        assertEquals("Cannot read source code", logTester.logs(LoggerLevel.WARN).get(1));

        // The default max-spaces-after parameter is 1 but as we construct the check ourselves
        // the SonarQube init process cannot happen and this is a simple int attribute init
        // so we expect an error to be found
        HyphensCheck check2 = new HyphensCheck();
        check2.setYamlSourceCode(code);
        check2.validate();
        assertTrue(check2.getYamlSourceCode().hasCorrectSyntax());
        assertEquals(1, check2.getYamlSourceCode().getYamlIssues().size());
        assertEquals("too many spaces after hyphen (hyphens)", check2.getYamlSourceCode().getYamlIssues().get(0).getMessage());
    }

    @Test
    public void testCreateViolation() throws IOException {
        DummyYamlCheck check = getDummyCheck();

        assertEquals(0, check.getYamlSourceCode().getYamlIssues().size());

        check.createViolation(new LintProblem(1, 1, "error 1"));
        assertEquals(1, check.getYamlSourceCode().getYamlIssues().size());
        assertEquals("error 1", check.getYamlSourceCode().getYamlIssues().get(0).getMessage());

        check.createViolation(new LintProblem(1, 1, "error 2"), false);
        assertEquals(2, check.getYamlSourceCode().getYamlIssues().size());
        assertEquals("error 1", check.getYamlSourceCode().getYamlIssues().get(0).getMessage());
        assertEquals("error 2", check.getYamlSourceCode().getYamlIssues().get(1).getMessage());
        assertTrue(check.getYamlSourceCode().hasCorrectSyntax());
        assertNull(check.getYamlSourceCode().getSyntaxError());

        check.createViolation(new LintProblem(1, 1, "error 3"), true);
        assertEquals(3, check.getYamlSourceCode().getYamlIssues().size());
        assertEquals("error 1", check.getYamlSourceCode().getYamlIssues().get(0).getMessage());
        assertEquals("error 2", check.getYamlSourceCode().getYamlIssues().get(1).getMessage());
        assertEquals("error 3", check.getYamlSourceCode().getYamlIssues().get(2).getMessage());
        assertFalse(check.getYamlSourceCode().hasCorrectSyntax());
        assertEquals("error 3", check.getYamlSourceCode().getSyntaxError().getMessage());
    }

    @Test
    public void testIsFileIncluded() throws IOException {
        DummyYamlCheck check = getDummyCheck();

        assertTrue(check.isFileIncluded(null));
        // False: need to take directories into account
        assertFalse(check.isFileIncluded("dummy-file.yaml"));

        // All other tests with directories
        assertTrue(check.isFileIncluded("**/dummy-file.yaml"));
        assertTrue(check.isFileIncluded("**/*dummy*"));
        assertTrue(check.isFileIncluded("**/files/dummy-*"));
        assertTrue(check.isFileIncluded("**/test/*/dummy-*.yaml"));
        assertTrue(check.isFileIncluded("**/src/**/dummy-*.yaml"));
        assertTrue(check.isFileIncluded("**/files/dummy-*.yaml"));
        assertFalse(check.isFileIncluded("**/files/dummy-*.yml"));
    }

    @Test
    public void testGetLintRuleId() {
        assertEquals("new-lines", new NewLinesCheck().getLintRuleId());
    }

    @Test
    public void testGetYamlLintconfig() throws YamlLintConfigException {
        new CommentsIndentationCheck().getYamlLintconfig();
        boolean found = false;
        for (String message : logTester.logs(LoggerLevel.DEBUG)) {
            if (!message.startsWith("Got RuleProperty ")) {
                found = true;
                assertThat(message, containsString("'---\n" +
                        "rules:\n" +
                        "  comments-indentation: enable'"));
            }
        }
        if (!found) {
            fail("Expected rule configuration YAML fragment");
        }

        logTester.clear();

        // The default max-spaces-after parameter is 1 but as we construct the check ourselves
        // the SonarQube init process cannot happen and this is a simple int attribute init
        new HyphensCheck().getYamlLintconfig();
        found = false;
        for (String message : logTester.logs(LoggerLevel.DEBUG)) {
            if (!message.startsWith("Got RuleProperty ")) {
                found = true;
                assertThat(message, containsString("'---\n" +
                        "rules:\n" +
                        "  hyphens:\n" +
                        "    max-spaces-after: 0\n'"));
            }
        }
        if (!found) {
            fail("Expected rule configuration YAML fragment");
        }
    }


    private DummyYamlCheck getDummyCheck() throws IOException {
        DummyYamlCheck check = new DummyYamlCheck();
        check.setYamlSourceCode(
                new YamlSourceCode(Utils.getInputFile("src" + File.separator + "test" + File.separator + "files" + File.separator + "dummy-file.yaml"))
        );

        return check;
    }

    private class DummyYamlCheck extends YamlLintCheck {
    }
}
