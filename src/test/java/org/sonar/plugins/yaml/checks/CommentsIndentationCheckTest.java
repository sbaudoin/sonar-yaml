package org.sonar.plugins.yaml.checks;

import junit.framework.TestCase;

public class CommentsIndentationCheckTest extends TestCase {
    public void testCheck() {
        assertNotNull(new CommentsIndentationCheck());
    }
}
