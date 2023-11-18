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

import com.github.sbaudoin.sonar.plugins.yaml.Utils;
import com.github.sbaudoin.sonar.plugins.yaml.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.yaml.checks.YamlSourceCode;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import com.github.sbaudoin.yamllint.Cli;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.AdditionalAnswers;
import org.mockito.MockedConstruction;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.internal.DefaultHighlighting;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.internal.SensorStorage;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Predicate;

import static com.github.sbaudoin.yamllint.Cli.XDG_CONFIG_HOME_ENV_VAR;
import static com.github.sbaudoin.yamllint.Cli.YAMLLINT_CONFIG_FILE_ENV_VAR;
import static com.github.stefanbirkner.systemlambda.SystemLambda.restoreSystemProperties;
import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class YamlSensorTest {
    private final RuleKey ruleKey = RuleKey.of(CheckRepository.REPOSITORY_KEY, "BracesCheck");
    private final String parsingErrorCheckKey = "ParsingErrorCheck";
    private final RuleKey parsingErrorCheckRuleKey = RuleKey.of(CheckRepository.REPOSITORY_KEY, parsingErrorCheckKey);

    private YamlSensor sensor;
    private SensorContextTester context;
    private DefaultFileSystem fs;


    @TempDir
    Path temporaryFolder;

    @RegisterExtension
    LogTesterJUnit5 logTester = new LogTesterJUnit5();


    @Test
    void testSensor1() throws Exception {
        init(false);
        fs.add(Utils.getInputFile("braces/min-spaces-02.yaml"));

        DummySensorDescriptor descriptor = new DummySensorDescriptor();
        sensor.describe(descriptor);
        assertEquals("YAML Sensor", descriptor.sensorName);
        assertEquals(YamlLanguage.KEY, descriptor.languageKey);

        sensor.execute(context);

        assertEquals(1, context.allIssues().size());
        context.allIssues().stream().forEach(issue -> {
            assertEquals(ruleKey, issue.ruleKey());
            assertEquals(2, issue.primaryLocation().textRange().start().line());
        });
    }

    @Test
    void testSensor2() throws Exception {
        withEnvironmentVariable("XDG_CONFIG_HOME", "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG").execute(() -> {
            init(false);
            fs.add(Utils.getInputFile("braces/min-spaces-02.yaml"));

            DummySensorDescriptor descriptor = new DummySensorDescriptor();
            sensor.describe(descriptor);
            sensor.execute(context);

            assertEquals(0, context.allIssues().size());
        });
    }

    /**
     * Test when YamlSourceCode throws an exception when constructed
     *
     * @throws Exception
     */
    @Test
    void testSensorIOException() throws Exception {
        init(false);

        InputFile inputFile = Utils.getInputFile("braces/min-spaces-02.yaml");
        fs.add(inputFile);
        Optional<Boolean> optional = Optional.empty();
        try(MockedConstruction<YamlSourceCode> mocked = mockConstructionWithAnswer(YamlSourceCode.class, invocation -> {
            throw new IOException("Boom!");
        })) {
            sensor.execute(context);
            assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
            assertEquals("Error reading source file min-spaces-02.yaml", logTester.logs(LoggerLevel.WARN).get(0));
            assertEquals(0, context.allIssues().size());
        }
    }

    @Test
    void testSensorSyntaxError() throws Exception {
        init(true);
        fs.add(Utils.getInputFile("braces/min-spaces-01.yaml"));

        sensor.execute(context);

        assertEquals(1, context.allIssues().size());
        context.allIssues().stream().forEach(issue -> {
            assertEquals(parsingErrorCheckRuleKey, issue.ruleKey());
            assertEquals("Parse error: syntax error: expected <block end>, but found '-'", issue.primaryLocation().message());
            assertEquals(4, issue.primaryLocation().textRange().start().line());
        });
    }

    @Test
    void testSensorHighlightingUnsupportedOperationException() throws Exception {
        init(false);

        SensorContextTester myContext = mock(SensorContextTester.class, AdditionalAnswers.delegatesTo(context));
        SensorStorage myStorage = mock(SensorStorage.class);
        NewHighlighting myHighlighting = mock(NewHighlighting.class, AdditionalAnswers.delegatesTo(new RedundantHighlighting(myStorage)));
        doReturn(myHighlighting).when(myContext).newHighlighting();

        InputFile inputFile = Utils.getInputFile("k8s.yml");
        fs.add(inputFile);

        sensor.execute(myContext);
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertEquals("Cannot save highlighting for file k8s.yml, ignoring", logTester.logs(LoggerLevel.WARN).get(0));
        assertEquals(0, myContext.allIssues().size());
    }

    @Test
    void testGlobalConfig0() throws Exception {
        init(false);
        assertNull(sensor.localConfig);
    }

    @Test
    void testGlobalConfig1() throws Exception {
        withEnvironmentVariable(YAMLLINT_CONFIG_FILE_ENV_VAR, null).
                and(Cli.XDG_CONFIG_HOME_ENV_VAR, "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG").execute(() -> {
                    init(false);
                    assertNotNull(sensor.localConfig);
                    assertNotNull(sensor.localConfig.getRuleConf("comments"));
                    assertNull(sensor.localConfig.getRuleConf("braces"));
                });
    }

    @Test
    void testGlobalConfig2() throws Exception {
        withEnvironmentVariable(YAMLLINT_CONFIG_FILE_ENV_VAR, null).and(XDG_CONFIG_HOME_ENV_VAR, null).execute(() ->  // Need clean XDG_CONFIG_HOME because it may be set on the test environment
                restoreSystemProperties(() -> {
                    System.setProperty("user.home", System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "home");
                    init(false);
                    assertNotNull(sensor.localConfig);
                    assertNull(sensor.localConfig.getRuleConf("comments"));
                    assertNotNull(sensor.localConfig.getRuleConf("braces"));
                })
        );
    }

    @Test
    void testGlobalConfig3() throws Exception {
        withEnvironmentVariable(Cli.YAMLLINT_CONFIG_FILE_ENV_VAR, "src" + File.separator + "test" + File.separator + "resources" + File.separator + "config" + File.separator + "XDG" + File.separator + "yamllint" + File.separator + "config").
                and(XDG_CONFIG_HOME_ENV_VAR, null).execute(() -> {
                    init(false);
                    assertNotNull(sensor.localConfig);
                    assertNotNull(sensor.localConfig.getRuleConf("comments"));
                    assertNull(sensor.localConfig.getRuleConf("braces"));
        });
    }

    @Test
    void testLocalConfig1() throws Exception {
        Files.copy(Paths.get("src", "test", "resources", "config", "local", Cli.USER_CONF_FILENAME), Utils.BASE_DIR.resolve(Cli.USER_CONF_FILENAME), StandardCopyOption.REPLACE_EXISTING);

        init(false);
        assertNotNull(sensor.localConfig);
        assertNull(sensor.localConfig.getRuleConf("comments"));
        assertNotNull(sensor.localConfig.getRuleConf("braces"));
    }

    @Test
    void testLocalConfig2() throws Exception {
        Files.copy(Paths.get("src", "test", "resources", "config", "local", Cli.USER_CONF_FILENAME), Utils.BASE_DIR.resolve(Cli.USER_CONF_FILENAME + ".yaml"), StandardCopyOption.REPLACE_EXISTING);

        init(false);
        assertNotNull(sensor.localConfig);
        assertNull(sensor.localConfig.getRuleConf("comments"));
        assertNotNull(sensor.localConfig.getRuleConf("braces"));
    }

    @Test
    void testLocalConfig3() throws Exception {
        Files.copy(Paths.get("src", "test", "resources", "config", "local", Cli.USER_CONF_FILENAME), Utils.BASE_DIR.resolve(Cli.USER_CONF_FILENAME + ".yml"), StandardCopyOption.REPLACE_EXISTING);

        init(false);
        assertNotNull(sensor.localConfig);
        assertNull(sensor.localConfig.getRuleConf("comments"));
        assertNotNull(sensor.localConfig.getRuleConf("braces"));
    }

    @AfterEach
    void cleanEnv() throws IOException {
        Files.deleteIfExists(Utils.BASE_DIR.resolve(Cli.USER_CONF_FILENAME));
        Files.deleteIfExists(Utils.BASE_DIR.resolve(Cli.USER_CONF_FILENAME + ".yaml"));
        Files.deleteIfExists(Utils.BASE_DIR.resolve(Cli.USER_CONF_FILENAME + ".yml"));
    }


    private void init(boolean activateParsingErrorCheck) throws Exception {
        context = Utils.getSensorContext();

        fs = Utils.getFileSystem();
        Path newFolder = temporaryFolder.resolveSibling("temp");
        newFolder.toFile().mkdir();
        fs.setWorkDir(newFolder);

        ActiveRules activeRules;
        if (activateParsingErrorCheck) {
            activeRules = new ActiveRulesBuilder()
                    .addRule(new NewActiveRule.Builder().setRuleKey(ruleKey).build())
                    .addRule(new NewActiveRule.Builder().setRuleKey(parsingErrorCheckRuleKey)
                             .setInternalKey(parsingErrorCheckKey).build())
                    .build();
        } else {
            activeRules = new ActiveRulesBuilder()
                    .addRule(new NewActiveRule.Builder().setRuleKey(ruleKey).build())
                    .build();
        }
        context.setActiveRules(activeRules);
        CheckFactory checkFactory = new CheckFactory(activeRules);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        sensor = new YamlSensor(fs, checkFactory, fileLinesContextFactory);
    }


    private static class DummySensorDescriptor implements SensorDescriptor {
        private String sensorName;
        private String languageKey;

        @Override
        public SensorDescriptor name(String sensorName) {
            this.sensorName = sensorName;
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguage(String languageKey) {
            this.languageKey = languageKey;
            return this;
        }

        @Override
        public SensorDescriptor onlyOnLanguages(String... languageKeys) {
            return this;
        }

        @Override
        public SensorDescriptor onlyOnFileType(InputFile.Type type) {
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepository(String... repositoryKey) {
            return this;
        }

        @Override
        public SensorDescriptor createIssuesForRuleRepositories(String... repositoryKeys) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperty(String... propertyKey) {
            return this;
        }

        @Override
        public SensorDescriptor requireProperties(String... propertyKeys) {
            return this;
        }

        @Override
        public SensorDescriptor global() {
            return this;
        }

        @Override
        public SensorDescriptor onlyWhenConfiguration(Predicate<Configuration> predicate) {
            return this;
        }
    }

    private static class RedundantHighlighting extends DefaultHighlighting {
        public RedundantHighlighting(SensorStorage storage) {
            super(storage);
        }

        @Override
        public void doSave() {
            // that's what would happen if some highlighting had already been saved for same file
            throw new UnsupportedOperationException("Blam!");
        }
    }
}
