package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class DocumentEndCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new DocumentEndCheck());
    }
}
