package com.github.sbaudoin.sonar.plugins.yaml;

import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlLanguage;
import com.github.sbaudoin.sonar.plugins.yaml.languages.YamlQualityProfile;
import com.github.sbaudoin.sonar.plugins.yaml.rules.YamlRulesDefinition;
import com.github.sbaudoin.sonar.plugins.yaml.rules.YamlSensor;
import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;
import org.sonar.api.Plugin;

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
