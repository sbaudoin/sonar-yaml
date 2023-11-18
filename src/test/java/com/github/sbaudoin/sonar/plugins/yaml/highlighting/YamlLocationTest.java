/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.yaml.highlighting;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.error.Mark;

import static org.junit.jupiter.api.Assertions.*;

class YamlLocationTest {
    @Test
    void testConstructors() {
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

    @Test
    void testToString() {
        String yaml = "---\nfoo: bar\n";
        assertEquals("{ content: \"" + yaml + "\"; line: 1; column: 1; characterOffset: 0 }", new YamlLocation(yaml).toString());
    }

    @Test
    void testShift() {
        String yaml = "---\nfoo: bar\n";
        YamlLocation location1 = new YamlLocation(yaml);
        YamlLocation location2 = location1.shift(7);
        assertEquals(2, location2.line());
        assertEquals(4, location2.column());
        assertTrue(location2.isSameAs(new YamlLocation(yaml, 1, 1, 7)));
        try {
            location2.shift(40);
            fail("Invalid shift value accepted");
        } catch (IllegalStateException e) {
            assertEquals("Cannot shift by 40 characters", e.getMessage());
        }
    }

    @Test
    void testMoveBefore() {
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
