package org.sonar.plugins.yaml.rules;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonarsource.analyzer.commons.RuleMetadataLoader;
import org.sonar.plugins.yaml.checks.CheckRepository;
import org.sonar.plugins.yaml.languages.YamlLanguage;

import java.util.ArrayList;
import java.util.List;

public class YamlRulesDefinition implements RulesDefinition {
    public static final String RULES_DEFINITION_FOLDER = "org/sonar/l10n/yaml/rules/yaml";


    @Override
    public void define(Context context) {
        NewRepository repository = context.createRepository(CheckRepository.REPOSITORY_KEY, YamlLanguage.KEY).setName(CheckRepository.REPOSITORY_NAME);

        RuleMetadataLoader metadataLoader = new RuleMetadataLoader(RULES_DEFINITION_FOLDER);
        List<Class> allCheckClasses = new ArrayList<>(CheckRepository.getCheckClasses());
        metadataLoader.addRulesByAnnotatedClass(repository, allCheckClasses);

        // Declare rule templates
        for(NewRule rule : repository.rules()) {
            if (CheckRepository.getTemplateRuleKeys().contains(rule.key())) {
                rule.setTemplate(true);
            }
        }

        repository.done();
    }
}
