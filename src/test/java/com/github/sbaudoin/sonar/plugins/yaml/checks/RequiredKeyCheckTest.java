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
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class RequiredKeyCheckTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testCheck() {
        assertNotNull(new RequiredKeyCheck());
    }

    @Test
    public void testFailedValidateNoSource() {
        try {
            new RequiredKeyCheck().validate();
            fail("No source code should raise an exception");
        } catch (IllegalStateException e) {
            assertEquals("Source code not set, cannot validate anything", e.getMessage());
        }
    }

    @Test
    public void testFailedValidateIOException() throws IOException {
        // Prepare error
        YamlSourceCode code = getSourceCode("required-key-01.yaml", false);
        YamlSourceCode spy = spy(code);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));

        RequiredKeyCheck check = new RequiredKeyCheck();
        check.keyName = "required";
        check.isKeyNameAtRoot = "yes";

        check.setYamlSourceCode(spy);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertThat(logTester.logs(LoggerLevel.WARN).get(0), containsString("Cannot read source code"));
        assertEquals(0, spy.getYamlIssues().size());
    }

    @Test
    public void testValidateSyntaxError() throws IOException {
        RequiredKeyCheck check = new RequiredKeyCheck();
        check.keyName = "required";
        check.isKeyNameAtRoot = "yes";

        // Syntax error
        YamlSourceCode code = getSourceCode("required-key-01.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertThat(logTester.logs(LoggerLevel.WARN).get(0), containsString("Syntax error found, cannot continue checking keys: syntax error: expected <block end>, but found '-'"));
        assertFalse(code.hasCorrectSyntax());
        assertNull(code.getSyntaxError().getRuleKey());
        assertEquals("syntax error: expected <block end>, but found '-'", code.getSyntaxError().getMessage());
        assertEquals(4, code.getSyntaxError().getLine());
        assertEquals(3, code.getSyntaxError().getColumn());
        assertEquals(0, code.getYamlIssues().size());
    }

    @Test
    public void testValidateNoIssue() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "yes", "readinessProbe");

        // Syntax 1
        YamlSourceCode code = getSourceCode("required-key-02.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());

        // Syntax 2
        code = getSourceCode("required-key-03.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
    }

    @Test
    public void testValidateWithRequiredKey1() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "yes", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-04.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(2, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
    }

    @Test
    public void testValidateWithRequiredKey2() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "yes", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(15, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
    }

    @Test
    public void testValidateWithRequiredKey3() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "yes", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-06.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());
        
        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(3, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());

        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(1).getMessage());
        assertEquals(25, code.getYamlIssues().get(1).getLine());
        assertEquals(1, code.getYamlIssues().get(1).getColumn());
    }

    @Test
    public void testValidateWithRequiredKey4() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "yes", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-07.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());

        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(3, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());

        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(60, code.getYamlIssues().get(1).getLine());
        assertEquals(1, code.getYamlIssues().get(1).getColumn());
    }

    @Test
    public void testValidateWithRequiredKey5() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "yes", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-08.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());
    }

    @Test
    public void testValidateWithRequiredKey6() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "not", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-08.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(9, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
    }

    private RequiredKeyCheck getRequiredCheck(String keyName, String keyValue, String isKeyNameAtRoot, String requiredKeyName) {
      RequiredKeyCheck check = new RequiredKeyCheck();
      check.keyName = keyName;
      check.keyValue = keyValue;
      check.isKeyNameAtRoot = isKeyNameAtRoot;
      check.requiredKeyName = requiredKeyName;
      return check;
    }

    private YamlSourceCode getSourceCode(String filename, boolean filter) throws IOException {
        return new YamlSourceCode(Utils.getInputFile("required-key/" + filename), Optional.of(filter));
    }
}
