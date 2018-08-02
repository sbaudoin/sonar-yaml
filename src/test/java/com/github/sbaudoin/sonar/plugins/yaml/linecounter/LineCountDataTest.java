package com.github.sbaudoin.sonar.plugins.yaml.linecounter;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.HashSet;

public class LineCountDataTest extends TestCase {
    public void testAll() {
        LineCountData lcd = new LineCountData(1, null, null);
        assertEquals(new Integer(1), lcd.linesNumber());
        assertNull(lcd.effectiveCommentLines());
        assertNull(lcd.linesOfCodeLines());

        lcd = new LineCountData(5, new HashSet(Arrays.asList(1, 2)), new HashSet(Arrays.asList(3, 4)));
        assertEquals(new Integer(5), lcd.linesNumber());
        assertEquals(new HashSet(Arrays.asList(3, 4)), lcd.effectiveCommentLines());
        assertEquals(new HashSet(Arrays.asList(1, 2)), lcd.linesOfCodeLines());
    }
}
