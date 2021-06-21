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
package com.github.sbaudoin.sonar.plugins.yaml.rules;

import com.github.sbaudoin.sonar.plugins.yaml.Utils;
import com.github.sbaudoin.sonar.plugins.yaml.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.yaml.checks.YamlSourceCode;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

@RunWith(PowerMockRunner.class)
@PrepareForTest(YamlSensor.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class YamlSensorTest {
    private final RuleKey ruleKey = RuleKey.of(CheckRepository.REPOSITORY_KEY, "BracesCheck");
    private final String parsingErrorCheckKey = "ParsingErrorCheck";
    private final RuleKey parsingErrorCheckRuleKey = RuleKey.of(CheckRepository.REPOSITORY_KEY, parsingErrorCheckKey);

    private YamlSensor sensor;
    private SensorContextTester context;
    private DefaultFileSystem fs;


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testSensor() throws Exception {
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
    public void testSensorIOException() throws Exception {
        init(false);

        InputFile inputFile = Utils.getInputFile("braces/min-spaces-02.yaml");
        fs.add(inputFile);
        Optional<Boolean> optional = Optional.empty();
        whenNew(YamlSourceCode.class).withArguments(inputFile, optional).thenThrow(new IOException("Boom!"));

        sensor.execute(context);
        assertEquals(1, logTester.logs(LoggerLevel.WARN).size());
        assertEquals("Error reading source file min-spaces-02.yaml", logTester.logs(LoggerLevel.WARN).get(0));
        assertEquals(0, context.allIssues().size());
    }

    @Test
    public void testSensorSyntaxError() throws Exception {
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


    private void init(boolean activateParsingErrorCheck) throws Exception {
        context = Utils.getSensorContext();

        fs = Utils.getFileSystem();
        fs.setWorkDir(temporaryFolder.newFolder("temp").toPath());

        ActiveRules activeRules = null;
        if (activateParsingErrorCheck) {
            activeRules = new ActiveRulesBuilder()
                    .create(ruleKey)
                    .activate()
                    .create(parsingErrorCheckRuleKey)
                    .setInternalKey(parsingErrorCheckKey)
                    .activate()
                    .build();
        } else {
            activeRules = new ActiveRulesBuilder()
                    .create(ruleKey)
                    .activate()
                    .build();
        }
        context.setActiveRules(activeRules);
        CheckFactory checkFactory = new CheckFactory(activeRules);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        sensor = new YamlSensor(fs, checkFactory, fileLinesContextFactory);
    }


    private class DummySensorDescriptor implements SensorDescriptor {
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
}
