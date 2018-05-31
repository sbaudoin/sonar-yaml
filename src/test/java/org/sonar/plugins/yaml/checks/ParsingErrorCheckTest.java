package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class ParsingErrorCheckTest extends TestCase {
    public void testCheck() {
        ParsingErrorCheck check = new ParsingErrorCheck();
        assertNotNull(check);
        try {
            check.validate();
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }
    }
}
