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
package com.github.sbaudoin.sonar.plugins.yaml.settings;

import org.junit.jupiter.api.Test;
import org.sonar.api.config.PropertyDefinition;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlLanguageSettingsTest {
    @Test
    void testGetPropertiesWithoutYamlBuiltinSupport() {
        List<PropertyDefinition> defs = YamlSettings.getProperties(false);

        assertEquals(3, defs.size());
        assertEquals(YamlSettings.FILE_SUFFIXES_KEY, defs.get(0).key());
        assertEquals(YamlSettings.FILE_SUFFIXES_DEFAULT_VALUE, defs.get(0).defaultValue());
        assertEquals(YamlSettings.FILTER_UTF8_LB_KEY, defs.get(1).key());
        assertEquals("false", defs.get(1).defaultValue());
        assertEquals(YamlSettings.YAML_LINT_CONF_PATH_KEY, defs.get(2).key());
        assertEquals("", defs.get(2).defaultValue());
    }

    @Test
    void testGetPropertiesWithYamlBuiltinSupport() {
        List<PropertyDefinition> defs = YamlSettings.getProperties(true);

        assertEquals(2, defs.size());
        assertEquals(YamlSettings.FILTER_UTF8_LB_KEY, defs.get(0).key());
        assertEquals("false", defs.get(0).defaultValue());
        assertEquals(YamlSettings.YAML_LINT_CONF_PATH_KEY, defs.get(1).key());
        assertEquals("", defs.get(1).defaultValue());
    }
}
