package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class ForbiddenKeyCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new ForbiddenKeyCheck());
    }
}
