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

import junit.framework.TestCase;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinition.Rule;
import com.github.sbaudoin.sonar.plugins.yaml.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;

public class YamlRulesDefinitionTest extends TestCase {
    public void testDefine() {
        YamlRulesDefinition rulesDefinition = new YamlRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        rulesDefinition.define(context);
        RulesDefinition.Repository repository = context.repository(CheckRepository.REPOSITORY_KEY);

        assertEquals(CheckRepository.REPOSITORY_NAME, repository.name());
        assertEquals(YamlLanguage.KEY, repository.language());
        assertEquals(CheckRepository.getCheckClasses().size(), repository.rules().size());

        RulesDefinition.Rule aRule = repository.rule("IndentationCheck");
        assertNotNull(aRule);
        assertEquals("For readability and maintenance YAML documents should have a consistent indentation", aRule.name());

        assertEquals(3L, repository.rules().stream().filter(Rule::template).map(Rule::key).count());

        for (Rule rule : repository.rules()) {
            for (RulesDefinition.Param param : rule.params()) {
                assertFalse("Description for " + param.key() + " should not be empty", "".equals(param.description()));
            }
        }
    }
}
