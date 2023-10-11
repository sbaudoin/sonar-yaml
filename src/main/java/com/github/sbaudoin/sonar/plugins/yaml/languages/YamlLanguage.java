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

import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes the YAML language and tells when it applies
 */
public class YamlLanguage extends AbstractLanguage {
    public static final String NAME = "YAML";
    public static final String KEY = "yaml";


    private final Configuration config;


    /**
     * Constructor
     *
     * @param config the SonarQube configuration for this language
     */
    public YamlLanguage(Configuration config) {
        super(KEY, NAME);
        this.config = config;
    }


    /**
     * Returns the file suffixes ({@code .yml} and {@code .yaml} by default) that identify YAML files
     *
     * @return a list of file suffixes that identify YAML files
     */
    @Override
    public String[] getFileSuffixes() {
        return getYamlFilesSuffixes(config);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns the suffixes of expected YAML files
     *
     * @param config the SonarQube configuration for this language
     * @return the list of expected suffixes (possibly the default ones)
     */
    public static String[] getYamlFilesSuffixes(Configuration config) {
        String[] suffixes = filterEmptyStrings(config.getStringArray(YamlSettings.FILE_SUFFIXES_KEY));
        if (suffixes.length == 0) {
            suffixes = StringUtils.split(YamlSettings.FILE_SUFFIXES_DEFAULT_VALUE, ",");
        }
        return suffixes;
    }


    /**
     * Cleans up the passed String array to remove empty strings, i.e. strings that are {@code null} or contain only
     * spaces
     *
     * @param stringArray an array of strings to be cleaned up
     * @return the cleaned up version of {@code stringArray}
     */
    private static String[] filterEmptyStrings(String[] stringArray) {
        List<String> nonEmptyStrings = new ArrayList<>();
        for (String string : stringArray) {
            if (StringUtils.isNotBlank(string.trim())) {
                nonEmptyStrings.add(string.trim());
            }
        }
        return nonEmptyStrings.toArray(new String[0]);
    }
}
