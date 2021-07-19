/**
 * Copyright (c) 2018-2020, Sylvain Baudoin
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
package com.github.sbaudoin.sonar.plugins.yaml;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlQualityProfile;
import com.github.sbaudoin.sonar.plugins.yaml.rules.YamlRulesDefinition;
import com.github.sbaudoin.sonar.plugins.yaml.rules.YamlSensor;
import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import org.slf4j.LoggerFactory;
import org.sonar.api.Plugin;
import org.sonar.api.utils.Version;

/**
 * Main plugin class
 */
public class YamlPlugin implements Plugin {
    static final Version SONARQUBE_WITH_YAML_SUPPORT_VERSION = Version.create(9, 1);

    public YamlPlugin() {
        // Disable INFO logs for Reflections (see )
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
        context.getLogger(org.reflections.Reflections.class).setLevel(Level.ERROR);
    }

    @Override
    public void define(Context context) {
        boolean hasBuiltinYamlSupport = hasBuiltinYamlLanguageSupport(context);
        if (!hasBuiltinYamlSupport) {
            context.addExtension(YamlLanguage.class);
        }
        context.addExtension(new YamlQualityProfile(hasBuiltinYamlSupport));

        // Add plugin settings (file extensions, etc.)
        context.addExtensions(YamlSettings.getProperties(hasBuiltinYamlSupport));

        context.addExtensions(YamlRulesDefinition.class, YamlSensor.class);
    }

    public static boolean hasBuiltinYamlLanguageSupport(Context context) {
        return context.getSonarQubeVersion().isGreaterThanOrEqual(SONARQUBE_WITH_YAML_SUPPORT_VERSION);
    }
}
