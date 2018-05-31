package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class OctalValuesCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new OctalValuesCheck());
    }
}
