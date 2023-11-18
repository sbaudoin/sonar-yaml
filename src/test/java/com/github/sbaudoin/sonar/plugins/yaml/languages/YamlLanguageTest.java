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
package com.github.sbaudoin.sonar.plugins.yaml.languages;

import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.resources.AbstractLanguage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlLanguageTest {
    private MapSettings settings;
    private YamlLanguage yaml;


    @BeforeEach
    void setUp() {
        settings = new MapSettings();
        yaml = new YamlLanguage(settings.asConfig());
    }

    @Test
    void defaultSuffixes() {
        settings.setProperty(YamlSettings.FILE_SUFFIXES_KEY, "");
        assertArrayEquals(new String[] { ".yaml", ".yml" }, yaml.getFileSuffixes());
    }

    @Test
    void customSuffixes() {
        settings.setProperty(YamlSettings.FILE_SUFFIXES_KEY, ".myYaml, ");
        assertArrayEquals(new String[] { ".myYaml" }, yaml.getFileSuffixes());
    }

    @Test
    void testEquals() {
        assertEquals(yaml, yaml);
        assertEquals(yaml, new YamlLanguage(settings.asConfig()));
        assertEquals(yaml, new FakeLanguage());
    }

    @Test
    void testHashCode() {
        assertEquals("yaml".hashCode(), yaml.hashCode());
    }


    private static class FakeLanguage extends AbstractLanguage {
        public FakeLanguage() {
            super("yaml");
        }

        @Override
        public String[] getFileSuffixes() {
            return new String[0];
        }
    }
}
