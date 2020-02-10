/**
 * Copyright (c) 2018-2020, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
