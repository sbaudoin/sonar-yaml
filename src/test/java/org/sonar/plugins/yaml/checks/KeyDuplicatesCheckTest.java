package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class KeyDuplicatesCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new KeyDuplicatesCheck());
    }
}
