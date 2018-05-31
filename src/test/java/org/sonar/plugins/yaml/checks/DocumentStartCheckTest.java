package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class DocumentStartCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new DocumentStartCheck());
    }
}
