package com.github.sbaudoin.sonar.plugins.yaml.checks;

import com.github.sbaudoin.sonar.plugins.yaml.Utils;
import com.github.sbaudoin.yamllint.LintProblem;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class YamlSourceCodeTest {
    InputFile inputFile;
    YamlSourceCode code;

    @Before
    public void setInputFile() throws IOException {
        inputFile = Utils.getInputFile("braces/min-spaces-01.yaml");
        code = new YamlSourceCode(inputFile, Optional.of(Boolean.FALSE));
    }

    @Test
    public void testGetYamlFile() throws IOException {
        assertEquals(inputFile, code.getYamlFile());
        assertEquals("---\n" +
                "dict: {}\n" +
                "fdgsfg: fgdfg\n" +
                "- fdgfd:\n" +
                "    fgdfg: fgfgfgf\n" +
                "    - fgf:\n" +
                "    # Comment", code.getContent());
    }

    @Test
    public void testSyntaxError() {
        assertFalse(code.hasCorrectSyntax());
        assertNotNull(code.getSyntaxError());
        assertNull(code.getSyntaxError().getRuleKey());
        assertEquals(4, code.getSyntaxError().getLine());
        assertEquals(1, code.getSyntaxError().getColumn());
        assertEquals("syntax error: expected <block end>, but found '-'", code.getSyntaxError().getMessage());
        assertTrue(code.getSyntaxError().isSyntaxError());
    }

    @Test
    public void testYamlIssue() {
        // There is a brace issue in the tested file but as the sensor has not run and no rule has been enabled, nothing can be returned here yet
        assertEquals(0, code.getYamlIssues().size());
        YamlLintIssue issue1 = new YamlLintIssue(new LintProblem(7, 2, null, "brace error"), null);
        code.addViolation(issue1);
        YamlIssue issue2 = new YamlIssue(null, "brace error", 2, 7);
        code.addViolation(issue2);
        assertEquals(2, code.getYamlIssues().size());
        assertEquals(issue1, code.getYamlIssues().get(0));
        assertEquals(issue2, code.getYamlIssues().get(1));
    }

    @Test
    public void testFilter() throws IOException {
        String code = "---\nlist: ['one',\u2028 'two']";
        InputFile file = Utils.getInputFile("dummy-file.yaml");
        InputFile spy = spy(file);
        when(spy.contents()).thenReturn(code);

        YamlSourceCode sourceCode;
        sourceCode = new YamlSourceCode(spy, Optional.empty());
        assertEquals(code.length(), sourceCode.getContent().length());
        sourceCode = new YamlSourceCode(spy, Optional.of(false));
        assertEquals(code.length(), sourceCode.getContent().length());
        sourceCode = new YamlSourceCode(spy, Optional.of(true));
        assertEquals(code.length() - 1, sourceCode.getContent().length());
    }
}
