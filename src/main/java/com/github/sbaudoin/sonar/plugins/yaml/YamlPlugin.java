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
package com.github.sbaudoin.sonar.plugins.yaml;

import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlQualityProfile;
import com.github.sbaudoin.sonar.plugins.yaml.rules.YamlRulesDefinition;
import com.github.sbaudoin.sonar.plugins.yaml.rules.YamlSensor;
import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import org.sonar.api.Plugin;

/**
 * Main plugin class
 */
public class YamlPlugin implements Plugin {
    @Override
    public void define(Context context) {
        context.addExtension(YamlLanguage.class);
        context.addExtension(YamlQualityProfile.class);

        // Add plugin settings (file extensions, etc.)
        context.addExtensions(YamlSettings.getProperties());

        context.addExtensions(YamlRulesDefinition.class, YamlSensor.class);
    }
}
