package org.sonar.plugins.yaml;

import org.sonar.api.Plugin;
import org.sonar.plugins.yaml.languages.YamlLanguage;
import org.sonar.plugins.yaml.languages.YamlQualityProfile;
import org.sonar.plugins.yaml.rules.YamlRulesDefinition;
import org.sonar.plugins.yaml.rules.YamlSensor;
import org.sonar.plugins.yaml.settings.YamlSettings;

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
