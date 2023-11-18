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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.log.LogTesterJUnit5;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YamlSensorNoRuleTest {
    @RegisterExtension
    LogTesterJUnit5 logTester = new LogTesterJUnit5();


    @Test
    void testNoActiveRule(@TempDir Path temporaryFolder) throws IOException {
        SensorContextTester context = Utils.getSensorContext();

        DefaultFileSystem fs = Utils.getFileSystem();
        Path newFolder = temporaryFolder.resolveSibling("temp");
        newFolder.toFile().mkdir();
        fs.setWorkDir(newFolder);
        context.setFileSystem(fs);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        InputFile playbook1 = Utils.getInputFile("braces/min-spaces-01.yaml");
        InputFile playbook2 = Utils.getInputFile("braces/min-spaces-02.yaml");
        InputFile playbook3 = Utils.getInputFile("dummy-file.yaml");
        context.fileSystem().add(playbook1).add(playbook2).add(playbook3);

        ActiveRules activeRules = new ActiveRulesBuilder()
                .addRule(new NewActiveRule.Builder().setRuleKey(RuleKey.of("foo", "bar")).build())
                .build();
        YamlSensor sensor = new YamlSensor(fs, new CheckFactory(activeRules), fileLinesContextFactory);

        sensor.execute(context);
        assertEquals(1, logTester.logs(LoggerLevel.INFO).size());
        assertEquals("No active rules found for this plugin, skipping.", logTester.logs(LoggerLevel.INFO).get(0));
        assertEquals(0, context.allIssues().size());
    }
}
