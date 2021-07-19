/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

        lcd = new LineCountData(5, new HashSet<Integer>(Arrays.asList(1, 2)), new HashSet<Integer>(Arrays.asList(3, 4)));
        assertEquals(new Integer(5), lcd.linesNumber());
        assertEquals(new HashSet<Integer>(Arrays.asList(3, 4)), lcd.effectiveCommentLines());
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2)), lcd.linesOfCodeLines());
    }
}
