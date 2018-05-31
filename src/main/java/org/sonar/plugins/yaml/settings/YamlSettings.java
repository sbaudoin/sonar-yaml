package org.sonar.plugins.yaml.settings;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

import static java.util.Arrays.asList;

public class YamlSettings {
    public static final String FILE_SUFFIXES_KEY = "sonar.yaml.file.suffixes";
    public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".yaml,.yml";


    private YamlSettings() {
    }


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
