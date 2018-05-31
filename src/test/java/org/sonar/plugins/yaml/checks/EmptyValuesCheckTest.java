package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class EmptyValuesCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new EmptyValuesCheck());
    }
}
