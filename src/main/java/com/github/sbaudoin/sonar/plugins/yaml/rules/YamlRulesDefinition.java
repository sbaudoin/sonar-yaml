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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;
import com.github.sbaudoin.sonar.plugins.yaml.checks.CheckRepository;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;

import java.util.ArrayList;
import java.util.List;

/**
 * Rules definition class for this plugin
 */
public class YamlRulesDefinition implements RulesDefinition {
    /**
     * Path to the directory/folder containing the descriptor files (JSON and HTML) for the rules
     */
    public static final String RULES_DEFINITION_FOLDER = "org/sonar/l10n/yaml/rules/yaml";


    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(CheckRepository.REPOSITORY_KEY, YamlLanguage.KEY).setName(CheckRepository.REPOSITORY_NAME);

        RuleMetadataLoader metadataLoader = new RuleMetadataLoader(RULES_DEFINITION_FOLDER);
        @SuppressWarnings("rawtypes")
        List<Class> allCheckClasses = new ArrayList<>(CheckRepository.getCheckClasses());
        metadataLoader.addRulesByAnnotatedClass(repository, allCheckClasses);

        // Declare rule templates
        for (NewRule rule : repository.rules()) {
            if (CheckRepository.getTemplateRuleKeys().contains(rule.key())) {
                rule.setTemplate(true);
            }
        }

        repository.done();
    }
}
