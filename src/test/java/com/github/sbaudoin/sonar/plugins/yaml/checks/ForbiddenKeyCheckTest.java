/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class ForbiddenKeyCheckTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testCheck() {
        assertNotNull(new ForbiddenKeyCheck());
    }

    @Test
    public void testFailedValidateNoSource() {
        try {
            new ForbiddenKeyCheck().validate();
            fail("No source code should raise an exception");
        } catch (IllegalStateException e) {
            assertEquals("Source code not set, cannot validate anything", e.getMessage());
        }
    }

    @Test
    public void testFailedValidateIOException() throws IOException {
        // Prepare error
        YamlSourceCode code = getSourceCode("forbidden-key-01.yaml", false);
        YamlSourceCode spy = spy(code);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));

        ForbiddenKeyCheck check = new ForbiddenKeyCheck();
        check.keyName = "forbidden";

        check.setYamlSourceCode(spy);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).contains("Cannot read source code"));
        assertEquals(0, spy.getYamlIssues().size());
    }

    @Test
    public void testValidateSyntaxError() throws IOException {
        ForbiddenKeyCheck check = new ForbiddenKeyCheck();
        check.keyName = "forbidden";

        // Syntax error
        YamlSourceCode code = getSourceCode("forbidden-key-01.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).contains("Syntax error found, cannot continue checking keys: syntax error: expected <block end>, but found '-'"));
        assertFalse(code.hasCorrectSyntax());
        assertNull(code.getSyntaxError().getRuleKey());
        assertEquals("syntax error: expected <block end>, but found '-'", code.getSyntaxError().getMessage());
        assertEquals(4, code.getSyntaxError().getLine());
        assertEquals(3, code.getSyntaxError().getColumn());
        assertEquals(0, code.getYamlIssues().size());
    }

    @Test
    public void testValidateNoIssue() throws IOException {
        ForbiddenKeyCheck check = new ForbiddenKeyCheck();
        check.keyName = "forbidden";

        // Syntax 1
        YamlSourceCode code = getSourceCode("forbidden-key-02.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // Syntax 2
        code = getSourceCode("forbidden-key-03.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // Syntax 3
        code = getSourceCode("forbidden-key-04.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());
    }

    @Test
    public void testValidateWithForbiddenKey1() throws IOException {
        ForbiddenKeyCheck check = new ForbiddenKeyCheck();
        check.keyName = "forbidden";

        YamlSourceCode code = getSourceCode("forbidden-key-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Forbidden key found", code.getYamlIssues().get(0).getMessage());
        assertEquals(4, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
    }

    @Test
    public void testValidateWithForbiddenKey2() throws IOException {
        ForbiddenKeyCheck check = new ForbiddenKeyCheck();
        check.keyName = "^forbid*en";

        YamlSourceCode code = getSourceCode("forbidden-key-06.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Forbidden key found", code.getYamlIssues().get(0).getMessage());
        assertEquals(4, code.getYamlIssues().get(0).getLine());
        assertEquals(5, code.getYamlIssues().get(0).getColumn());
    }

    @Test
    public void testValidateWithForbiddenKey3() throws IOException {
        ForbiddenKeyCheck check = new ForbiddenKeyCheck();
        check.keyName = "^forbidden.*";
        check.includedAncestors = "<root>:nesting1:nesting2.*";
        check.excludedAncestors = ".*:nesting2:nesting3";

        YamlSourceCode code = getSourceCode("forbidden-key-07.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());
        assertEquals("Forbidden key found", code.getYamlIssues().get(0).getMessage());
        assertEquals(5, code.getYamlIssues().get(0).getLine());
        assertEquals(5, code.getYamlIssues().get(0).getColumn());
        assertEquals(6, code.getYamlIssues().get(1).getLine());
        assertEquals(5, code.getYamlIssues().get(1).getColumn());
    }

    private YamlSourceCode getSourceCode(String filename, boolean filter) throws IOException {
        return new YamlSourceCode(Utils.getInputFile("forbidden-key/" + filename), Optional.of(filter));
    }
}
