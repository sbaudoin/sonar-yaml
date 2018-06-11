package com.github.sbaudoin.sonar.plugins.yaml.linecounter;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.io.Serializable;

public class LineCounter {
    private static final Logger LOGGER = Loggers.get(LineCounter.class);

    /**
     * Hide constructor
     */
    private LineCounter() {
    }


    public static void analyse(SensorContext context, FileLinesContextFactory fileLinesContextFactory, InputFile inputFile) {
        LOGGER.debug("Count lines in {}", inputFile.filename());

        try {
            saveMeasures(
                    inputFile,
                    new LineCountParser(inputFile.contents()).getLineCountData(),
                    fileLinesContextFactory.createFor(inputFile), context);
        } catch (IOException e) {
            LOGGER.warn("Unable to count lines for file " + inputFile.filename() + ", ignoring measures", e);
        }
    }

    private static void saveMeasures(InputFile yamlFile, LineCountData data, FileLinesContext fileLinesContext, SensorContext context) {
        for (int line = 1; line <= data.linesNumber(); line++) {
            fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, data.linesOfCodeLines().contains(line) ? 1 : 0);
            fileLinesContext.setIntValue(CoreMetrics.COMMENT_LINES_DATA_KEY, line, data.effectiveCommentLines().contains(line) ? 1 : 0);
        }
        fileLinesContext.save();

        saveMeasure(context, yamlFile, CoreMetrics.COMMENT_LINES, data.effectiveCommentLines().size());
        saveMeasure(context, yamlFile, CoreMetrics.NCLOC, data.linesOfCodeLines().size());
    }

    private static <T extends Serializable> void saveMeasure(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
        context.<T>newMeasure()
                .withValue(value)
                .forMetric(metric)
                .on(inputFile)
                .save();
    }
}
