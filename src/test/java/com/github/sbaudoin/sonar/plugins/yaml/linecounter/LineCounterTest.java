package com.github.sbaudoin.sonar.plugins.yaml.linecounter;

import com.github.sbaudoin.sonar.plugins.yaml.Utils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class LineCounterTest {
    private FileLinesContextFactory fileLinesContextFactory;
    private FileLinesContext fileLinesContext;

    @Rule
    public LogTester logTester = new LogTester();

    @Before
    public void init() {
        fileLinesContextFactory = mock(FileLinesContextFactory.class);
        fileLinesContext = mock(FileLinesContext.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(fileLinesContext);
    }

    @Test
    public void testNormal() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        String filePath = "dummy-file.yaml";
        InputFile inputFile = Utils.getInputFile(filePath);
        LineCounter.analyse(context, fileLinesContextFactory, inputFile);
        assertEquals(new Integer(2), context.measure(getComponentKey(filePath), CoreMetrics.NCLOC).value());
        assertEquals(new Integer(2), context.measure(getComponentKey(filePath), CoreMetrics.COMMENT_LINES).value());
    }

    @Test
    public void testIOException() throws IOException {
        SensorContextTester context = Utils.getSensorContext();
        InputFile inputFile = Utils.getInputFile("dummy-file.yaml");
        InputFile spy = spy(inputFile);
        when(spy.contents()).thenThrow(new IOException("Cannot read file"));

        LineCounter.analyse(context, fileLinesContextFactory, spy);
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertEquals("Unable to count lines for file " + inputFile.filename() + ", ignoring measures", logTester.logs(LoggerLevel.WARN).get(0));
    }

    private String getComponentKey(String filePath) {
        return Utils.MODULE_KEY + ":src/test/resources/" + filePath;
    }
}
