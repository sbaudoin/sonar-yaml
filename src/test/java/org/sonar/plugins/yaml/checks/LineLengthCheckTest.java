package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class LineLengthCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new LineLengthCheck());
    }
}
