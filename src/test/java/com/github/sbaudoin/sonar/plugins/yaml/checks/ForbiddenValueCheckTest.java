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

public class ForbiddenValueCheckTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testCheck() {
        assertNotNull(new ForbiddenValueCheck());
    }

    @Test
    public void testFailedValidateNoSource() {
        try {
            new ForbiddenValueCheck().validate();
            fail("No source code should raise an exception");
        } catch (IllegalStateException e) {
            assertEquals("Source code not set, cannot validate anything", e.getMessage());
        }
    }

    @Test
    public void testFailedValidateIOException() throws IOException {
        // Prepare error
        YamlSourceCode code = getSourceCode("forbidden-value-01.yaml", false);
        YamlSourceCode spy = spy(code);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));

        ForbiddenValueCheck check = new ForbiddenValueCheck();
        check.keyName = "forbidden";
        check.value = "forbidden";

        check.setYamlSourceCode(spy);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).contains("Cannot read source code"));
        assertEquals(0, spy.getYamlIssues().size());
    }

    @Test
    public void testValidateSyntaxError() throws IOException {
        ForbiddenValueCheck check = new ForbiddenValueCheck();
        check.keyName = "forbidden";
        check.value = "forbidden";

        // Syntax error
        YamlSourceCode code = getSourceCode("forbidden-value-01.yaml", false);
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
        ForbiddenValueCheck check = new ForbiddenValueCheck();
        check.keyName = "forbidden";
        check.value = "forbidden";

        // Syntax 1
        YamlSourceCode code = getSourceCode("forbidden-value-02.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // Syntax 2
        code = getSourceCode("forbidden-value-03.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // Syntax 3
        code = getSourceCode("forbidden-value-04.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());
    }

    @Test
    public void testValidateWithForbiddenKey1() throws IOException {
        ForbiddenValueCheck check = new ForbiddenValueCheck();
        check.keyName = "forbidden";
        check.value = "^forbidden$";

        YamlSourceCode code = getSourceCode("forbidden-value-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());
        assertEquals("Forbidden value found", code.getYamlIssues().get(0).getMessage());
        assertEquals(5, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(1).getMessage());
        assertEquals(7, code.getYamlIssues().get(1).getLine());
        assertEquals(19, code.getYamlIssues().get(1).getColumn());
    }

    @Test
    public void testValidateWithForbiddenKey2() throws IOException {
        ForbiddenValueCheck check = new ForbiddenValueCheck();
        check.keyName = "forbidden";
        check.value = "forbidden";

        YamlSourceCode code = getSourceCode("forbidden-value-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(5, code.getYamlIssues().size());
        assertEquals("Forbidden value found", code.getYamlIssues().get(0).getMessage());
        assertEquals(5, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(1).getMessage());
        assertEquals(7, code.getYamlIssues().get(1).getLine());
        assertEquals(19, code.getYamlIssues().get(1).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(2).getMessage());
        assertEquals(9, code.getYamlIssues().get(2).getLine());
        assertEquals(3, code.getYamlIssues().get(2).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(2).getMessage());
        assertEquals(14, code.getYamlIssues().get(3).getLine());
        assertEquals(3, code.getYamlIssues().get(3).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(3).getMessage());
        assertEquals(19, code.getYamlIssues().get(4).getLine());
        assertEquals(3, code.getYamlIssues().get(4).getColumn());
    }

    @Test
    public void testValidateWithForbiddenKey3() throws IOException {
        ForbiddenValueCheck check = new ForbiddenValueCheck();
        check.keyName = "^forbid*en";
        check.value = ".*forbidden.*";

        YamlSourceCode code = getSourceCode("forbidden-value-06.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(4, code.getYamlIssues().size());
        assertEquals("Forbidden value found", code.getYamlIssues().get(0).getMessage());
        assertEquals(5, code.getYamlIssues().get(0).getLine());
        assertEquals(5, code.getYamlIssues().get(0).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(1).getMessage());
        assertEquals(7, code.getYamlIssues().get(1).getLine());
        assertEquals(21, code.getYamlIssues().get(1).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(2).getMessage());
        assertEquals(13, code.getYamlIssues().get(2).getLine());
        assertEquals(5, code.getYamlIssues().get(2).getColumn());
        assertEquals("Forbidden value found", code.getYamlIssues().get(3).getMessage());
        assertEquals(21, code.getYamlIssues().get(3).getLine());
        assertEquals(5, code.getYamlIssues().get(3).getColumn());
    }

    @Test
    public void testValidateWithForbiddenValue4() throws IOException {
        ForbiddenValueCheck check = new ForbiddenValueCheck();
        check.keyName = "connect(ion)?-?[tT]imeout.*";
        check.value = "^(\\d\\d\\d\\d|[7-9]\\d\\d)$"; // >= 700 ms
        check.excludedAncestors = ".*datasource:hikari";

        YamlSourceCode code = getSourceCode("forbidden-value-07.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Forbidden value found", code.getYamlIssues().get(0).getMessage());
        assertEquals(4, code.getYamlIssues().get(0).getLine());
        assertEquals(5, code.getYamlIssues().get(0).getColumn());
    }
    private YamlSourceCode getSourceCode(String filename, boolean filter) throws IOException {
        return new YamlSourceCode(Utils.getInputFile("forbidden-value/" + filename), Optional.of(filter));
    }
}
