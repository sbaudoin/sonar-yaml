package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class BracesCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new BracesCheck());
    }
}
