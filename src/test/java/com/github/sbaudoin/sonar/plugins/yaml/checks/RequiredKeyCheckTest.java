/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class RequiredKeyCheckTest {
    @RegisterExtension
    LogTesterJUnit5 logTester = new LogTesterJUnit5();

    @Test
    void testCheck() {
        assertNotNull(new RequiredKeyCheck());
    }

    @Test
    void testFailedValidateNoSource() {
        RequiredKeyCheck c = new RequiredKeyCheck();
        try {
            c.validate();
            fail("No source code should raise an exception");
        } catch (IllegalStateException e) {
            assertEquals("Source code not set, cannot validate anything", e.getMessage());
        }
    }

    @Test
    void testFailedValidateIOException() throws IOException {
        // Prepare error
        YamlSourceCode code = getSourceCode("required-key-01.yaml", false);
        YamlSourceCode spy = spy(code);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));

        RequiredKeyCheck check = new RequiredKeyCheck();
        check.parentKeyName = "required";
        check.isParentKeyAtRoot = "yes";

        check.setYamlSourceCode(spy);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).contains("Cannot read source code"));
        assertEquals(0, spy.getYamlIssues().size());
    }

    @Test
    void testValidateSyntaxError() throws IOException {
        RequiredKeyCheck check = new RequiredKeyCheck();
        check.parentKeyName = "required";
        check.isParentKeyAtRoot = "yes";

        // Syntax error
        YamlSourceCode code = getSourceCode("required-key-01.yaml", false);
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
    void testValidateNoIssue() throws IOException {
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
    void testValidateWithRequiredKey1() throws IOException {
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
    void testValidateWithRequiredKey2() throws IOException {
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
    void testValidateWithRequiredKey3() throws IOException {
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
    void testValidateWithRequiredKey4() throws IOException {
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
    void testValidateWithRequiredKey5() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Deployment", "yes", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-08.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());
    }

    @Test
    void testValidateWithRequiredKey6() throws IOException {
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

    @Test
    void testValidateWithRequiredKey7() throws IOException {
        RequiredKeyCheck check = getRequiredCheck("kind", "Pod", "yes", "readinessProbe");

        YamlSourceCode code = getSourceCode("required-key-09.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());

        assertEquals("Required readinessProbe key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(25, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
    }

    @Test
    void testValidateWithRequiredKeyAncestors1() throws IOException {
        RequiredKeyCheck check = new RequiredKeyCheck();
        check.parentKeyName = "";//"parent1";
        check.parentKeyValue = "";//"value1";
        check.isParentKeyAtRoot = "";//"yes";
        check.requiredKeyName = "required.*";
        check.includedAncestors = ".*:nesting\\d";
        check.excludedAncestors = ".*:nesting2:nesting3";

        YamlSourceCode code = getSourceCode("required-key-10.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());

        assertEquals("Required required.* key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(3, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
        assertEquals("Required required.* key not found", code.getYamlIssues().get(1).getMessage());
        assertEquals(17, code.getYamlIssues().get(1).getLine());
        assertEquals(1, code.getYamlIssues().get(1).getColumn());
    }

    @Test
    void testValidateWithRequiredKeyAncestors2() throws IOException {
        RequiredKeyCheck check = new RequiredKeyCheck();
        check.parentKeyName = "parent1";
        check.parentKeyValue = "value1";
        check.isParentKeyAtRoot = "yes";
        check.requiredKeyName = "required.*";
        check.includedAncestors = ".*:nesting\\d";
        check.excludedAncestors = ".*:nesting2:nesting3";

        YamlSourceCode code = getSourceCode("required-key-11.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());

        assertEquals("Required required.* key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(3, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
        assertEquals("Required required.* key not found", code.getYamlIssues().get(1).getMessage());
        assertEquals(17, code.getYamlIssues().get(1).getLine());
        assertEquals(1, code.getYamlIssues().get(1).getColumn());
    }

    @Test
    void testValidateWithRequiredKeyAncestors3() throws IOException {
        RequiredKeyCheck check = new RequiredKeyCheck();
        check.parentKeyName = "parent1";
        check.parentKeyValue = "value2";
        check.isParentKeyAtRoot = "yes";
        check.requiredKeyName = "required.*";
        check.includedAncestors = "<root>:nesting\\d";
        check.excludedAncestors = ".*:nesting2:nesting3";

        YamlSourceCode code = getSourceCode("required-key-12.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

    }
    @Test
    void testValidateWithRequiredKeyAncestors4() throws IOException {
        RequiredKeyCheck check = new RequiredKeyCheck();
        check.parentKeyName = "parent2";
        check.parentKeyValue = "value2";
        check.isParentKeyAtRoot = "yes";
        check.requiredKeyName = "required.*";
        check.includedAncestors = ".*:nesting\\d";
        check.excludedAncestors = ".*:nesting2:nesting3";

        YamlSourceCode code = getSourceCode("required-key-13.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Required required.* key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(17, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
    }

    @Test
    void testValidateWithRequiredKeyAncestors5() throws IOException {
        RequiredKeyCheck check = new RequiredKeyCheck();
        check.parentKeyName = "";
        check.parentKeyValue = "";
        check.isParentKeyAtRoot = "";
        check.requiredKeyName = "waitDurationInOpenState.*|wait-duration-in-open-state.*";
        check.includedAncestors = ".*:circuitbreaker";
        check.excludedAncestors = "";

        YamlSourceCode code = getSourceCode("required-key-14.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Required waitDurationInOpenState.*|wait-duration-in-open-state.* key not found", code.getYamlIssues().get(0).getMessage());
        assertEquals(3, code.getYamlIssues().get(0).getLine());
        assertEquals(1, code.getYamlIssues().get(0).getColumn());
    }


    private RequiredKeyCheck getRequiredCheck(String parentKeyName, String parentKeyValue, String isParentKeyAtRoot, String requiredKeyName) {
      RequiredKeyCheck check = new RequiredKeyCheck();
      check.parentKeyName = parentKeyName;
      check.parentKeyValue = parentKeyValue;
      check.isParentKeyAtRoot = isParentKeyAtRoot;
      check.requiredKeyName = requiredKeyName;
      return check;
    }

    private YamlSourceCode getSourceCode(String filename, boolean filter) throws IOException {
        return new YamlSourceCode(Utils.getInputFile("required-key/" + filename), Optional.of(filter));
    }
}
