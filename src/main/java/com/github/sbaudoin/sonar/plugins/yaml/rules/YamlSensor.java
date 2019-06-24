/**
 * Copyright (c) 2018-2019, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.yaml.rules;

import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.github.sbaudoin.sonar.plugins.yaml.linecounter.LineCounter;
import com.github.sbaudoin.sonar.plugins.yaml.checks.*;
import com.github.sbaudoin.sonar.plugins.yaml.highlighting.HighlightingData;
import com.github.sbaudoin.sonar.plugins.yaml.highlighting.YamlHighlighting;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Main sensor
 */
public class YamlSensor implements Sensor {
    private static final Logger LOGGER = Loggers.get(YamlSensor.class);

    private final Checks<Object> checks;
    private final FileSystem fileSystem;
    private final FilePredicate mainFilesPredicate;
    private final FileLinesContextFactory fileLinesContextFactory;


    /**
     * Constructor
     *
     * @param fileSystem the file system on which the sensor will find the files to be analyzed
     * @param checkFactory check factory used to get the checks to execute against the files
     * @param fileLinesContextFactory factory used to report measures
     */
    public YamlSensor(FileSystem fileSystem, CheckFactory checkFactory, FileLinesContextFactory fileLinesContextFactory) {
        this.fileLinesContextFactory = fileLinesContextFactory;
        this.checks = checkFactory.create(CheckRepository.REPOSITORY_KEY).addAnnotatedChecks((Iterable<?>) CheckRepository.getCheckClasses());
        this.fileSystem = fileSystem;
        this.mainFilesPredicate = fileSystem.predicates().and(
                fileSystem.predicates().hasType(InputFile.Type.MAIN),
                fileSystem.predicates().hasLanguage(YamlLanguage.KEY));
    }


    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(YamlLanguage.KEY);
        descriptor.name("YAML Sensor");
    }

    @Override
    public void execute(SensorContext context) {
        LOGGER.debug("YAML sensor executed with context: " + context);
        Optional<RuleKey> parsingErrorKey = getParsingErrorRuleKey();

        // Skip analysis if no rules enabled from this plugin
        if (context.activeRules().findByRepository(CheckRepository.REPOSITORY_KEY).isEmpty()) {
            LOGGER.info("No active rules found for this plugin, skipping.");
            return;
        }

        for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
            LOGGER.debug("Analyzing file: " + inputFile.filename());
            try {
                YamlSourceCode sourceCode = new YamlSourceCode(inputFile, context.config().getBoolean(YamlSettings.FILTER_UTF8_LB_KEY));

                // First check for syntax errors
                if (!sourceCode.hasCorrectSyntax()) {
                    LOGGER.debug("File has syntax errors");
                    processAnalysisError(context, sourceCode, inputFile, parsingErrorKey);
                }

                computeLinesMeasures(context, sourceCode);
                runChecks(context, sourceCode);
            } catch (IOException e) {
                LOGGER.warn("Error reading source file " + inputFile.filename(), e);
            }
        }
    }


    /**
     * Calculates and feeds line measures (comments, actual number of code lines)
     *
     * @param context the sensor context
     * @param sourceCode the YAML source code to be analyzed
     */
    private void computeLinesMeasures(SensorContext context, YamlSourceCode sourceCode) {
        LineCounter.analyse(context, fileLinesContextFactory, sourceCode);
    }

    /**
     * Returns the {@link RuleKey} of the check that tags syntax errors
     *
     * @return a {@link RuleKey}
     */
    private Optional<RuleKey> getParsingErrorRuleKey() {
        for (Object obj : checks.all()) {
            YamlCheck check = (YamlCheck) obj;
            if (check.getClass().equals(CheckRepository.getParsingErrorCheckClass())) {
                LOGGER.debug("Parsing error rule key found: " + check.getRuleKey());
                return Optional.of(checks.ruleKey(check));
            }
        }
        LOGGER.debug("No parsing error rule key found");
        return Optional.empty();
    }

    /**
     * Runs all checks (except the syntax check) against the passed YAML source code
     *
     * @param context the sensor context
     * @param sourceCode the source code to be checked
     */
    private void runChecks(SensorContext context, YamlSourceCode sourceCode) {
        for (Object check : checks.all()) {
            ((YamlCheck) check).setRuleKey(checks.ruleKey(check));
            ((YamlCheck) check).setYamlSourceCode(sourceCode);
            LOGGER.debug("Checking rule: " + ((YamlCheck) check).getRuleKey());
            ((YamlCheck) check).validate();
        }
        saveIssues(context, sourceCode);
        try {
            saveSyntaxHighlighting(context, sourceCode);
        } catch (IOException e) {
            throw new IllegalStateException("Could not analyze file " + sourceCode.getYamlFile().filename(), e);
        }
    }

    /**
     * Saves the found issues in SonarQube
     *
     * @param context the context
     * @param sourceCode the analyzed YAML source
     */
    private void saveIssues(SensorContext context, YamlSourceCode sourceCode) {
        for (YamlIssue yamlIssue : sourceCode.getYamlIssues()) {
            LOGGER.debug("Saving issue: " + yamlIssue.getMessage());
            NewIssue newIssue = context.newIssue().forRule(yamlIssue.getRuleKey());
            NewIssueLocation location = newIssue.newLocation()
                    .on(sourceCode.getYamlFile())
                    .message(yamlIssue.getMessage())
                    .at(sourceCode.getYamlFile().selectLine(yamlIssue.getLine()==0?1:yamlIssue.getLine()));
            newIssue.at(location).save();
        }
    }

    /**
     * Saves the syntax highlighting for the analyzed code
     *
     * @param context the sensor context
     * @param sourceCode the YAML source code
     */
    private static void saveSyntaxHighlighting(SensorContext context, YamlSourceCode sourceCode) throws IOException {
        List<HighlightingData> highlightingDataList = new YamlHighlighting(sourceCode).getHighlightingData();
        NewHighlighting highlighting = context.newHighlighting().onFile(sourceCode.getYamlFile());

        for (HighlightingData highlightingData : highlightingDataList) {
            highlightingData.highlight(highlighting);
        }
        highlighting.save();
    }

    /**
     * Reports the passed issue as a syntax/parse error (aka {@link org.sonar.api.batch.sensor.error.AnalysisError} in
     * the SonarQube terminology)
     *
     * @param context the sensor context
     * @param sourceCode the analyzed YAML source
     * @param inputFile the file that contains the error
     * @param parsingErrorKey the {@link RuleKey} of the check that corresponds to a syntax error. If present, an issue
     *                        is reported as well as the analysis error.
     */
    private static void processAnalysisError(SensorContext context, YamlSourceCode sourceCode, InputFile inputFile, Optional<RuleKey> parsingErrorKey) {
        final YamlIssue error = sourceCode.getSyntaxError();

        LOGGER.warn("Syntax error in file: {}", inputFile.filename());
        LOGGER.warn("Cause: {} at line {}, column {}", error.getMessage(), error.getLine(), error.getColumn());

        LOGGER.debug("Creating analysis error");
        context.newAnalysisError()
                .onFile(inputFile)
                .message(sourceCode.getSyntaxError().getMessage())
                .at(new TextPointer() {
                    @Override
                    public int line() {
                        return error.getLine();
                    }

                    @Override
                    public int lineOffset() {
                        return error.getColumn();
                    }

                    @Override
                    public int compareTo(TextPointer textPointer) {
                        return textPointer.line() - line();
                    }
                })
                .save();

        if (parsingErrorKey.isPresent()) {
            LOGGER.debug("parsingErrorKey present, creating issue");
            // the ParsingErrorCheck rule is activated: we create a beautiful issue
            NewIssue newIssue = context.newIssue().forRule(parsingErrorKey.get());
            NewIssueLocation location = newIssue.newLocation()
                    .message("Parse error: " + error.getMessage())
                    .on(inputFile)
                    .at(sourceCode.getYamlFile().selectLine(error.getLine()));
            newIssue.at(location).save();
        }
    }
}
