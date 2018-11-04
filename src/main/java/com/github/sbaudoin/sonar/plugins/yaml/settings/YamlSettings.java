/**
 * Copyright (c) 2018, Sylvain Baudoin
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

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Wrapper class for the class settings
 */
public class YamlSettings {
    public static final String FILE_SUFFIXES_KEY = "sonar.yaml.file.suffixes";
    public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".yaml,.yml,.raml";


    /**
     * Hide constructor
     */
    private YamlSettings() {
    }


    /**
     * Returns the configuration properties of the plugin
     *
     * @return the configuration properties of the plugin
     */
    public static List<PropertyDefinition> getProperties() {
        return asList(PropertyDefinition.builder(FILE_SUFFIXES_KEY)
                .name("File Suffixes")
                .description("Comma-separated list of suffixes for files to analyze.")
                .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
                .multiValues(true)
                .category("YAML")
                .onQualifiers(Qualifiers.PROJECT)
                .build());
    }
}
