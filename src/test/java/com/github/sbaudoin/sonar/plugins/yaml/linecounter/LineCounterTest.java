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
package com.github.sbaudoin.sonar.plugins.yaml.linecounter;

import com.github.sbaudoin.sonar.plugins.yaml.Utils;
import com.github.sbaudoin.sonar.plugins.yaml.checks.YamlSourceCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LineCounterTest {
    private FileLinesContextFactory fileLinesContextFactory, bogusFileLinesContextFactory;
    private MyFileLinesContext fileLinesContext, bogusFileLinesContext;

    @RegisterExtension
    LogTesterJUnit5 logTester = new LogTesterJUnit5();

    @BeforeEach
    void init() {
        // Working factory
        fileLinesContextFactory = mock(FileLinesContextFactory.class);
        fileLinesContext = new MyFileLinesContext();
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);

        // Bogus factory
        bogusFileLinesContextFactory = mock(FileLinesContextFactory.class);
        bogusFileLinesContext = new MyBogusFileLinesContext();
        when(bogusFileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(bogusFileLinesContext);
    }

    @Test
    void testNormal() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        String filePath = "dummy-file.yaml";
        InputFile inputFile = Utils.getInputFile(filePath);
        LineCounter.analyse(context, fileLinesContextFactory, new YamlSourceCode(inputFile, Optional.of(false)));
        assertEquals(new Integer(2), context.measure(getComponentKey(filePath), CoreMetrics.NCLOC).value());
        assertEquals(new Integer(2), context.measure(getComponentKey(filePath), CoreMetrics.COMMENT_LINES).value());
    }

    @Test
    void testIOException() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        InputFile inputFile = Utils.getInputFile("dummy-file.yaml");
        YamlSourceCode sourceCode = new YamlSourceCode(inputFile, Optional.of(false));
        YamlSourceCode spy = spy(sourceCode);
        when(spy.getContent()).thenThrow(new IOException("Cannot read file"));

        LineCounter.analyse(context, fileLinesContextFactory, spy);
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertEquals("Unable to count lines for file " + inputFile.filename() + ", ignoring measures", logTester.logs(LoggerLevel.WARN).get(0));
    }

    @Test
    void testUnsupportedOperationException() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        InputFile inputFile = Utils.getInputFile("dummy-file.yaml");
        YamlSourceCode sourceCode = new YamlSourceCode(inputFile, Optional.of(false));

        LineCounter.analyse(context, bogusFileLinesContextFactory, sourceCode);
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertEquals("Cannot save measures for file " + inputFile.filename() + ", ignoring them", logTester.logs(LoggerLevel.WARN).get(0));
    }


    private String getComponentKey(String filePath) {
        return Utils.MODULE_KEY + ":src/test/resources/" + filePath;
    }


    private static class MyFileLinesContext implements FileLinesContext {
        Map<String, Map<Integer, Integer>> intValues = new HashMap<>();


        @Override
        public void setIntValue(String metricKey, int line, int value) {
            if (intValues.containsKey(metricKey)) {
                intValues.get(metricKey).put(line, value);
            } else {
                Map<Integer, Integer> values = new HashMap<>();
                values.put(line, value);
                intValues.put(metricKey, values);
            }
        }

        public Integer getIntValue(String metricKey, int line) {
            if (intValues.containsKey(metricKey)) {
                return intValues.get(metricKey).getOrDefault(line, -1);
            } else {
                return -1;
            }
        }

        @Override
        public void setStringValue(String metricKey, int line, String value) {

        }

        public String getStringValue(String metricKey, int line) {
            return null;
        }

        @Override
        public void save() {

        }
    }

    private static class MyBogusFileLinesContext extends MyFileLinesContext {
        @Override
        public void save() {
            throw new UnsupportedOperationException("Measure already set");
        }
    }
}
