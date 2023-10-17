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
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class IntValueInRangeCheckTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testCheck() {
        assertNotNull(new IntValueInRangeCheck());
    }

    @Test
    public void testFailedValidateNoSource() {
        IntValueInRangeCheck c = new IntValueInRangeCheck();
        try {
            c.validate();
            fail("No source code should raise an exception");
        } catch (IllegalStateException e) {
            assertEquals("Source code not set, cannot validate anything", e.getMessage());
        }
    }

    @Test
    public void testFailedValidateIOException() throws IOException {
        // Prepare error
        YamlSourceCode code = getSourceCode("int-value-in-range-01.yaml", false);
        YamlSourceCode spy = spy(code);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));

        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.keyName = "inRange";
        check.minValue = 1;
        check.maxValue = 5;

        check.setYamlSourceCode(spy);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).contains("Cannot read source code"));
        assertEquals(0, spy.getYamlIssues().size());
    }

    @Test
    public void testValidateSyntaxError() throws IOException {
        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.keyName = "inRange";
        check.minValue = 1;
        check.maxValue = 5;

        // Syntax error
        YamlSourceCode code = getSourceCode("int-value-in-range-01.yaml", false);
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
        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.keyName = "inRange";
        check.minValue = 1;
        check.maxValue = 5;

        // Syntax 1
        YamlSourceCode code = getSourceCode("int-value-in-range-02.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // Syntax 2
        code = getSourceCode("int-value-in-range-03.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // Syntax 3
        code = getSourceCode("int-value-in-range-04.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());
    }

    @Test
    public void testValidateParseError() throws IOException {
        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.keyName = "parseError\\d";
        check.minValue = 1;
        check.maxValue = 5;

        YamlSourceCode code = getSourceCode("int-value-in-range-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(3, code.getYamlIssues().size());
        assertEquals("Parse error: Non-integer value found for int-value-range-check", code.getYamlIssues().get(0).getMessage());
        assertEquals(9, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
        assertEquals("Parse error: Non-integer value found for int-value-range-check", code.getYamlIssues().get(1).getMessage());
        assertEquals(10, code.getYamlIssues().get(1).getLine());
        assertEquals(3, code.getYamlIssues().get(1).getColumn());
        assertEquals("Parse error: Non-integer value found for int-value-range-check", code.getYamlIssues().get(2).getMessage());
        assertEquals(11, code.getYamlIssues().get(2).getLine());
        assertEquals(3, code.getYamlIssues().get(2).getColumn());
    }

    @Test
    public void testValidateOutOfRange1() throws IOException {
        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.keyName = "inRange";
        check.minValue = 1;
        check.maxValue = 5;

        YamlSourceCode code = getSourceCode("int-value-in-range-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());
        assertEquals("Value out of range found. Range: min=" + check.minValue + " max=" + check.maxValue, code.getYamlIssues().get(0).getMessage());
        assertEquals(5, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
        assertEquals("Value out of range found. Range: min=" + check.minValue + " max=" + check.maxValue, code.getYamlIssues().get(1).getMessage());
        assertEquals(7, code.getYamlIssues().get(1).getLine());
        assertEquals(19, code.getYamlIssues().get(1).getColumn());
    }


    @Test
    public void testValidateOutOfRange2() throws IOException {
        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.includedAncestors = "<root>:key1";
        check.keyName = ".*Range";
        check.minValue = 1;
        check.maxValue = 5;

        YamlSourceCode code = getSourceCode("int-value-in-range-06.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());
        assertEquals("Value out of range found. Range: min=" + check.minValue + " max=" + check.maxValue, code.getYamlIssues().get(0).getMessage());
        assertEquals(5, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
        assertEquals("Value out of range found. Range: min=" + check.minValue + " max=" + check.maxValue, code.getYamlIssues().get(1).getMessage());
        assertEquals(7, code.getYamlIssues().get(1).getLine());
        assertEquals(19, code.getYamlIssues().get(1).getColumn());
    }

    @Test
    public void testValidateOutOfRange3() throws IOException {
        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.keyName = "connect(ion)?-?[tT]imeout.*";
        //check.includedAncestors = "<root>:[a-z\\-]+service[a-z0-9\\-]+:[a-z\\-]+endpoint[a-z0-9\\-]+";
        check.excludedAncestors = ".*datasource:hikari";
        check.minValue = 50;
        check.maxValue = 699;

        YamlSourceCode code = getSourceCode("int-value-in-range-07.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Value out of range found. Range: min=" + check.minValue + " max=" + check.maxValue, code.getYamlIssues().get(0).getMessage());
        assertEquals(4, code.getYamlIssues().get(0).getLine());
        assertEquals(5, code.getYamlIssues().get(0).getColumn());
    }

    @Test
    public void testValidateOutOfRange4() throws IOException {
        IntValueInRangeCheck check = new IntValueInRangeCheck();
        check.keyName = "connect(ion)?-?[tT]imeout.*";
        check.includedAncestors = "<root>:[a-z\\-]+service[a-z0-9\\-]+:[a-z\\-]+endpoint[a-z0-9\\-]+";
        //check.excludedAncestors = ".*datasource:hikari";
        check.minValue = 50;
        check.maxValue = 699;

        YamlSourceCode code = getSourceCode("int-value-in-range-07.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
        assertEquals("Value out of range found. Range: min=" + check.minValue + " max=" + check.maxValue, code.getYamlIssues().get(0).getMessage());
        assertEquals(4, code.getYamlIssues().get(0).getLine());
        assertEquals(5, code.getYamlIssues().get(0).getColumn());
    }

    private YamlSourceCode getSourceCode(String filename, boolean filter) throws IOException {
        return new YamlSourceCode(Utils.getInputFile("int-value-in-range/" + filename), Optional.of(filter));
    }
}
