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
package com.github.sbaudoin.sonar.plugins.yaml.languages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import org.sonar.api.resources.AbstractLanguage;

public class YamlLanguageTest {
    private MapSettings settings;
    private YamlLanguage yaml;


    @Before
    public void setUp() {
        settings = new MapSettings();
        yaml = new YamlLanguage(settings.asConfig());
    }

    @Test
    public void defaultSuffixes() {
        settings.setProperty(YamlSettings.FILE_SUFFIXES_KEY, "");
        Assert.assertArrayEquals(new String[] { ".yaml", ".yml" }, yaml.getFileSuffixes());
    }

    @Test
    public void customSuffixes() {
        settings.setProperty(YamlSettings.FILE_SUFFIXES_KEY, ".myYaml, ");
        Assert.assertArrayEquals(new String[] { ".myYaml" }, yaml.getFileSuffixes());
    }

    @Test
    public void testEquals() {
        Assert.assertFalse(yaml.equals("foo"));
        Assert.assertTrue(yaml.equals(yaml));
        Assert.assertTrue(yaml.equals(new YamlLanguage(settings.asConfig())));
        Assert.assertTrue(yaml.equals(new FakeLanguage()));
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals("yaml".hashCode(), yaml.hashCode());
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
