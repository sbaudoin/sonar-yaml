package com.github.sbaudoin.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class CheckRepositoryTest extends TestCase {
    public void testGetParsingErrorCheckClass() {
        assertEquals(ParsingErrorCheck.class, CheckRepository.getParsingErrorCheckClass());
    }

    public void testGetCheckClasses() {
        assertEquals(22, CheckRepository.getCheckClasses().size());
        assertTrue(CheckRepository.getCheckClasses().contains(ParsingErrorCheck.class));
    }

    public void testGetTemplateRuleKeys() {
        assertEquals(1, CheckRepository.getTemplateRuleKeys().size());
        assertTrue(CheckRepository.getTemplateRuleKeys().contains("ForbiddenKeyCheck"));
    }
}
