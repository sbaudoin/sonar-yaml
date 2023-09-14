/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Non-regression test for issue sbaudoin/sonar-yaml#27
 */
public class YamlSensorEmptyFileTest {
    private final RuleKey ruleKey = RuleKey.of(CheckRepository.REPOSITORY_KEY, "DocumentEndCheck");


    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testSensor() throws Exception {
        SensorContextTester context = Utils.getSensorContext();

        DefaultFileSystem fs = Utils.getFileSystem();
        fs.setWorkDir(temporaryFolder.newFolder("temp").toPath());

        ActiveRules activeRules = new ActiveRulesBuilder()
                .create(ruleKey)
                .setParam("present", "true")
                .activate()
                .build();
        context.setActiveRules(activeRules);
        CheckFactory checkFactory = new CheckFactory(activeRules);

        FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
        when(fileLinesContextFactory.createFor(any(InputFile.class))).thenReturn(mock(FileLinesContext.class));

        YamlSensor sensor = new YamlSensor(fs, checkFactory, fileLinesContextFactory);

        fs.add(Utils.getInputFile("empty.yaml"));

        sensor.execute(context);

        //assertEquals(1, context.allIssues().size());
        context.allIssues().stream().forEach(issue -> {
            assertEquals(ruleKey, issue.ruleKey());
            assertEquals(1, issue.primaryLocation().textRange().start().line());
            assertEquals(0, issue.primaryLocation().textRange().start().lineOffset());
        });
    }
}
