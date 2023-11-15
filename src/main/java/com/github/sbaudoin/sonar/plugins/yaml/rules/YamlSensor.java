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
package com.github.sbaudoin.sonar.plugins.yaml.rules;

import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.sbaudoin.yamllint.Cli.*;

/**
 * Main sensor
 */
public class YamlSensor implements Sensor {
    private static final Logger LOGGER = Loggers.get(YamlSensor.class);

    private final Checks<Object> checks;
    private final FileSystem fileSystem;
    private final FilePredicate mainFilesPredicate;
    private final FileLinesContextFactory fileLinesContextFactory;
    private List<String> expectedSuffixes = null;

    /**
     * Pointer to the YAMLLint configuration
     */
    protected final YamlLintConfig localConfig;

    /**
     * Name of a local rule configuration file: if this file is present in the work directory, it is used as an extension
     * configuration file
     */
    public static final String USER_CONF_FILENAME = ".yamllint";


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
        this.localConfig = getLocalConfig();
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
        boolean skipChecks = false;
        if (context.activeRules().findByRepository(CheckRepository.REPOSITORY_KEY).isEmpty()) {
            LOGGER.info("No active rules found for this plugin, skipping.");
            skipChecks = true;
        }

        for (InputFile inputFile : fileSystem.inputFiles(mainFilesPredicate)) {
            if (!fileHasExpectedSuffix(inputFile, context)) {
                LOGGER.debug("File " + inputFile.filename() + " does not have an expected suffix, ignoring it");
                continue;
            }

            LOGGER.debug("Analyzing file: " + inputFile.filename());
            try {
                YamlSourceCode sourceCode = new YamlSourceCode(inputFile, context.config().getBoolean(YamlSettings.FILTER_UTF8_LB_KEY));
                computeLinesMeasures(context, sourceCode);
                saveSyntaxHighlighting(context, sourceCode);

                if (!skipChecks) {
                    // First check for syntax errors
                    if (!sourceCode.hasCorrectSyntax()) {
                        LOGGER.debug("File has syntax errors");
                        processAnalysisError(context, sourceCode, inputFile, parsingErrorKey);
                    }
                    runChecks(context, sourceCode);
                }
            } catch (IOException e) {
                LOGGER.warn("Error reading source file " + inputFile.filename(), e);
            }
        }
    }


    /**
     * Tells if the passed file has a suffix expected as per the plugin configuration
     *
     * @param inputFile the file to check
     * @param context the runtime context (used to get the plugin configuration)
     * @return {@code true} if the file's suffix matches one of those configured, {@code false} if not
     */
    private boolean fileHasExpectedSuffix(InputFile inputFile, SensorContext context) {
        if (expectedSuffixes == null) {
            expectedSuffixes = Arrays.asList(YamlLanguage.getYamlFilesSuffixes(context.config()));
        }
        return expectedSuffixes.stream().anyMatch(s -> inputFile.filename().endsWith(s));
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
            setConfig(((YamlCheck) check));
            ((YamlCheck) check).validate();
        }
        saveIssues(context, sourceCode);
    }

    /**
     * Checks if there is a custom, local yamllint configuration file and returns the corresponding {@code YamlLintConfig}
     *
     * @return the {@code YamlLintConfig} that corresponds to the local yamllint configuration file or {@code null} if the
     * file does not exist or is invalid
     */
    private YamlLintConfig getLocalConfig() {
        Path userGlobalConfig = getUserGlobalConfigPath();

        try {
            if (fileExists(fileSystem.resolvePath(USER_CONF_FILENAME))) {
                return new YamlLintConfig(fileSystem.resolvePath(USER_CONF_FILENAME).toURI().toURL());
            } else if (fileExists(fileSystem.resolvePath(USER_CONF_FILENAME + ".yaml"))) {
                return new YamlLintConfig(fileSystem.resolvePath(USER_CONF_FILENAME + ".yaml").toURI().toURL());
            } else if (fileExists(fileSystem.resolvePath(USER_CONF_FILENAME + ".yml"))) {
                return new YamlLintConfig(fileSystem.resolvePath(USER_CONF_FILENAME + ".yml").toURI().toURL());
            } else if (fileExists(userGlobalConfig.toString())) {
                return new YamlLintConfig(userGlobalConfig.toUri().toURL());
            }
        } catch (IOException e) {
            LOGGER.warn("Cannot read yamllint user configuration file: " + e.getMessage());
            LOGGER.warn("Enable debug mode to get the complete stacktrace.");
            LOGGER.debug("Complete error trace:", e);
        } catch (YamlLintConfigException e) {
            LOGGER.warn("Configuration error: " + e.getMessage());
            LOGGER.warn("Enable debug mode to get the complete stacktrace.");
            LOGGER.debug("Complete error trace:", e);
        }
        return null;
    }

    /**
     * Checks if there is a configuration for the passed rule in the yamllint configuration file so that the rule will
     * use it instead of its SonarQube configuration
     *
     * @param check the rule to be configured with the local configuration
     */
    private void setConfig(YamlCheck check) {
        if (localConfig != null) {
            check.setConfig(localConfig);
        }
    }

    /**
     * Returns the path to the user's yamllint global configuration file, as per the environment setting
     *
     * @return the path to the user's global configuration file for yamllint
     */
    private Path getUserGlobalConfigPath() {
        Path userGlobalConfig;

        if (System.getenv(YAMLLINT_CONFIG_FILE_ENV_VAR) != null) {
            userGlobalConfig = Paths.get(System.getenv(YAMLLINT_CONFIG_FILE_ENV_VAR));
        } else if (System.getenv(XDG_CONFIG_HOME_ENV_VAR) != null) {
            userGlobalConfig = Paths.get(System.getenv(XDG_CONFIG_HOME_ENV_VAR), APP_NAME, "config");
        } else {
            userGlobalConfig = Paths.get(System.getProperty("user.home"), ".config", APP_NAME, "config");
        }

        return userGlobalConfig;
    }

    /**
     * Tells if the passed path is a file that exists
     *
     * @param path a path
     * @return <code>true</code> if the path exists and is a file, <code>false</code> otherwise
     */
    private boolean fileExists(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }

    /**
     * Tells if the passed path is a file that exists
     *
     * @param path a path
     * @return <code>true</code> if the path exists and is a file, <code>false</code> otherwise
     */
    private boolean fileExists(File path) {
        return path.exists() && path.isFile();
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
    private static void saveSyntaxHighlighting(SensorContext context, YamlSourceCode sourceCode) {
        List<HighlightingData> highlightingDataList;
        try {
            highlightingDataList = new YamlHighlighting(sourceCode).getHighlightingData();
        } catch (IOException e) {
            throw new IllegalStateException("Could not analyze file " + sourceCode.getYamlFile().filename(), e);
        }
        NewHighlighting highlighting = context.newHighlighting().onFile(sourceCode.getYamlFile());

        for (HighlightingData highlightingData : highlightingDataList) {
            highlightingData.highlight(highlighting);
        }
        try {
            highlighting.save();
        } catch (UnsupportedOperationException e) {
            String msg = "Cannot save highlighting for file " + sourceCode.getYamlFile().filename() + ", ignoring";
            if (LOGGER.isDebugEnabled()) {
                LOGGER.warn(msg, e);
            } else {
                LOGGER.warn(msg);
            }
            return;
        }
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
