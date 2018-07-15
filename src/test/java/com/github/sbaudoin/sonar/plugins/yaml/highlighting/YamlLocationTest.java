package com.github.sbaudoin.sonar.plugins.yaml.highlighting;

import junit.framework.TestCase;
import org.yaml.snakeyaml.error.Mark;

public class YamlLocationTest extends TestCase {
    public void testConstructors() {
        String yaml = "---\nfoo: bar\n";

        YamlLocation location1 = new YamlLocation(yaml);
        assertEquals(1, location1.line());
        assertEquals(1, location1.column());
        assertTrue(location1.isSameAs(new YamlLocation(yaml, 1, 1, 0)));

        YamlLocation location2 = new YamlLocation(yaml, new Mark("test", 0, 0, 0, yaml.toCharArray(), 0));
        assertEquals(1, location2.line());
        assertEquals(1, location2.column());
        assertTrue(location1.isSameAs(location2));
    }

    public void testToString() {
        String yaml = "---\nfoo: bar\n";
        assertEquals("{ content: \"" + yaml + "\"; line: 1; column: 1; characterOffset: 0 }", new YamlLocation(yaml).toString());
    }

    public void testShift() {
        String yaml = "---\nfoo: bar\n";
        YamlLocation location1 = new YamlLocation(yaml);
        YamlLocation location2 = location1.shift(7);
        assertEquals(2, location2.line());
        assertEquals(4, location2.column());
        assertTrue(location2.isSameAs(new YamlLocation(yaml, 1, 1, 7)));
    }

    public void testMoveBefore() {
        String yaml = "---\nfoo: bar\n";
        YamlLocation location1 = new YamlLocation(yaml);
        YamlLocation location2 = location1.moveBefore("bar");
        assertEquals(2, location2.line());
        assertEquals(6, location2.column());
        assertTrue(location2.isSameAs(new YamlLocation(yaml, 1, 1, 9)));

        try {
            location2.moveBefore("foo");
            fail("string not found in the remaining buffer should raise an exception");
        } catch (IllegalStateException e) {
            assertTrue(true);
        }
    }
}
