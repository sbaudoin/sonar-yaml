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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class DurationInRangeCheckTest {
    @RegisterExtension
    LogTesterJUnit5 logTester = new LogTesterJUnit5();

    @Test
    void testCheck() {
        assertNotNull(new DurationInRangeCheck());
    }

    @Test
    void testFailedValidateNoSource() {
        DurationInRangeCheck c = new DurationInRangeCheck();
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
        YamlSourceCode code = getSourceCode("duration-in-range-01.yaml", false);
        YamlSourceCode spy = spy(code);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));

        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "inRange";
        check.minMillis = 1;
        check.maxMillis = 5;

        check.setYamlSourceCode(spy);
        check.validate();
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertTrue(logTester.logs(LoggerLevel.WARN).get(0).contains("Cannot read source code"));
        assertEquals(0, spy.getYamlIssues().size());
    }

    @Test
    void testValidateSyntaxError() throws IOException {
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "inRange";
        check.minMillis = 1;
        check.maxMillis = 5;

        // Syntax error
        YamlSourceCode code = getSourceCode("duration-in-range-01.yaml", false);
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
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "inRange";
        check.minMillis = 1;
        check.maxMillis = 5;

        // Syntax 1
        YamlSourceCode code = getSourceCode("duration-in-range-02.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // Syntax 2
        code = getSourceCode("duration-in-range-03.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(0, code.getYamlIssues().size());

        // out of range
        code = getSourceCode("duration-in-range-04.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(1, code.getYamlIssues().size());
    }

    @Test
    void testValidateParseError() throws IOException {
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "parseError\\d";
        check.minMillis = 1;
        check.maxMillis = 5;

        YamlSourceCode code = getSourceCode("duration-in-range-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(3, code.getYamlIssues().size());
        assertEquals("Parse error: Non-duration found for duration-in-range-check", code.getYamlIssues().get(0).getMessage());
        assertEquals(9, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
        assertEquals("Parse error: Non-duration found for duration-in-range-check", code.getYamlIssues().get(1).getMessage());
        assertEquals(10, code.getYamlIssues().get(1).getLine());
        assertEquals(3, code.getYamlIssues().get(1).getColumn());
        assertEquals("Parse error: Non-duration found for duration-in-range-check", code.getYamlIssues().get(2).getMessage());
        assertEquals(11, code.getYamlIssues().get(2).getLine());
        assertEquals(3, code.getYamlIssues().get(2).getColumn());
    }

    @Test
    void testValidateOutOfRange1() throws IOException {
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "inRange";
        check.minMillis = 1;
        check.maxMillis = 5;

        YamlSourceCode code = getSourceCode("duration-in-range-05.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());

        assertEquals(check.getMessage(Duration.of(7, ChronoUnit.MILLIS)), code.getYamlIssues().get(0).getMessage());
        assertEquals(5, code.getYamlIssues().get(0).getLine());
        assertEquals(3, code.getYamlIssues().get(0).getColumn());
    }


    @Test
    void testValidateOutOfRange2() throws IOException {
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.includedAncestors = "<root>:key\\d";
        check.keyName = ".*Range";
        check.minMillis = 1;
        check.maxMillis = 5;

        YamlSourceCode code = getSourceCode("duration-in-range-06.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());

        YamlIssue yi = code.getYamlIssues().get(0);
        assertEquals(check.getMessage(Duration.of(0, ChronoUnit.MILLIS)), yi.getMessage());
        assertEquals(7, yi.getLine());
        assertEquals(19, yi.getColumn());

        yi = code.getYamlIssues().get(1);
        assertEquals(check.getMessage(Duration.of(7, ChronoUnit.MILLIS)), yi.getMessage());
        assertEquals(13, yi.getLine());
        assertEquals(3, yi.getColumn());

        yi = code.getYamlIssues().get(2);
        assertEquals(check.getMessage(Duration.of(3, ChronoUnit.SECONDS)), yi.getMessage());
        assertEquals(15, yi.getLine());
        assertEquals(3, yi.getColumn());

        yi = code.getYamlIssues().get(3);
        assertEquals(check.getMessage(Duration.of(7000, ChronoUnit.MICROS)), yi.getMessage());
        assertEquals(17, yi.getLine());
        assertEquals(3, yi.getColumn());

        yi = code.getYamlIssues().get(4);
        assertEquals(check.getMessage(Duration.of(2, ChronoUnit.MINUTES)), yi.getMessage());
        assertEquals(23, yi.getLine());
        assertEquals(3, yi.getColumn());

        yi = code.getYamlIssues().get(5);
        assertEquals(check.getMessage(Duration.of(100, ChronoUnit.MICROS)), yi.getMessage());
        assertEquals(25, yi.getLine());
        assertEquals(3, yi.getColumn());
    }

    @Test
    void testValidateOutOfRange3() throws IOException {
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "connect(ion)?-?[tT]imeout.*";
        //check.includedAncestors = "<root>:[a-z\\-]+service[a-z0-9\\-]+:[a-z\\-]+endpoint[a-z0-9\\-]+";
        check.excludedAncestors = ".*datasource:hikari";
        check.minMillis = 50;
        check.maxMillis = 699;

        YamlSourceCode code = getSourceCode("duration-in-range-07.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(3, code.getYamlIssues().size());

        YamlIssue yi = code.getYamlIssues().get(0);
        assertEquals(check.getMessage(Duration.of(700, ChronoUnit.MILLIS)), yi.getMessage());
        assertEquals(4, yi.getLine());
        assertEquals(5, yi.getColumn());

        yi = code.getYamlIssues().get(1);
        assertEquals(check.getMessage(Duration.of(700, ChronoUnit.MILLIS)), yi.getMessage());
        assertEquals(5, yi.getLine());
        assertEquals(5, yi.getColumn());

        yi = code.getYamlIssues().get(2);
        assertEquals(check.getMessage(Duration.of(1, ChronoUnit.SECONDS)), yi.getMessage());
        assertEquals(6, yi.getLine());
        assertEquals(5, yi.getColumn());
    }

    @Test
    void testValidateOutOfRange4() throws IOException {
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "connect(ion)?-?[tT]imeout.*";
        check.includedAncestors = "<root>:[a-z\\-]+service[a-z0-9\\-]+:[a-z\\-]+endpoint[a-z0-9\\-]+";
        //check.excludedAncestors = ".*datasource:hikari";
        check.minMillis = 50;
        check.maxMillis = 699;

        YamlSourceCode code = getSourceCode("duration-in-range-07.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(3, code.getYamlIssues().size());

        YamlIssue yi = code.getYamlIssues().get(0);
        assertEquals(check.getMessage(Duration.of(700, ChronoUnit.MILLIS)), yi.getMessage());
        assertEquals(4, yi.getLine());
        assertEquals(5, yi.getColumn());

        yi = code.getYamlIssues().get(1);
        assertEquals(check.getMessage(Duration.of(700, ChronoUnit.MILLIS)), yi.getMessage());
        assertEquals(5, yi.getLine());
        assertEquals(5, yi.getColumn());

        yi = code.getYamlIssues().get(2);
        assertEquals(check.getMessage(Duration.of(1, ChronoUnit.SECONDS)), yi.getMessage());
        assertEquals(6, yi.getLine());
        assertEquals(5, yi.getColumn());
    }

    @Test
    void testValidateOutOfRange5() throws IOException {
        DurationInRangeCheck check = new DurationInRangeCheck();
        check.keyName = "waitDurationInOpenState|maxWaitDurationInHalfOpenState";
        check.includedAncestors = "<root>:resilience4j:circuitbreaker.*";
        //check.excludedAncestors = ".*datasource:hikari";
        check.minMillis = 2000;
        check.maxMillis = 60000;

        YamlSourceCode code = getSourceCode("duration-in-range-08.yaml", false);
        check.setYamlSourceCode(code);
        check.validate();
        assertTrue(code.hasCorrectSyntax());
        assertEquals(2, code.getYamlIssues().size());

        YamlIssue yi = code.getYamlIssues().get(0);
        assertEquals(check.getMessage(Duration.of(1, ChronoUnit.SECONDS)), yi.getMessage());
        assertEquals(21, yi.getLine());
        assertEquals(9, yi.getColumn());

        yi = code.getYamlIssues().get(1);
        assertEquals(check.getMessage(Duration.of(5, ChronoUnit.MINUTES)), yi.getMessage());
        assertEquals(24, yi.getLine());
        assertEquals(9, yi.getColumn());
    }
    private YamlSourceCode getSourceCode(String filename, boolean filter) throws IOException {
        return new YamlSourceCode(Utils.getInputFile("duration-in-range/" + filename), Optional.of(filter));
    }
}
