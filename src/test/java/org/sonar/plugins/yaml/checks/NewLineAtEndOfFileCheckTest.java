package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class NewLineAtEndOfFileCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new NewLineAtEndOfFileCheck());
    }
}
