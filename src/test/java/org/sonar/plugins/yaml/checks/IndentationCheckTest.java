package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class IndentationCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new IndentationCheck());
    }
}
