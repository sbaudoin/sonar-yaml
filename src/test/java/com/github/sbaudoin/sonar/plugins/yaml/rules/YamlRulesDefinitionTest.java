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

        assertEquals(1L, repository.rules().stream().filter(Rule::template).map(Rule::key).count());

        for (Rule rule : repository.rules()) {
            for (RulesDefinition.Param param : rule.params()) {
                assertFalse("Description for " + param.key() + " should not be empty", "".equals(param.description()));
            }
        }
    }
}
