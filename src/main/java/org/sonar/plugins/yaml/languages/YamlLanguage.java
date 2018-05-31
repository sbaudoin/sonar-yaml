package org.sonar.plugins.yaml.languages;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.plugins.yaml.settings.YamlSettings;

import java.util.ArrayList;
import java.util.List;

public class YamlLanguage extends AbstractLanguage {
    public static final String NAME = "YAML";
    public static final String KEY = "yaml";


    private final Configuration config;


    public YamlLanguage(Configuration config) {
        super(KEY, NAME);
        this.config = config;
    }


    @Override
    public String[] getFileSuffixes() {
        String[] suffixes = filterEmptyStrings(config.getStringArray(YamlSettings.FILE_SUFFIXES_KEY));
        if (suffixes.length == 0) {
            suffixes = StringUtils.split(YamlSettings.FILE_SUFFIXES_DEFAULT_VALUE, ",");
        }
        return suffixes;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    private String[] filterEmptyStrings(String[] stringArray) {
        List<String> nonEmptyStrings = new ArrayList<>();
        for (String string : stringArray) {
            if (StringUtils.isNotBlank(string.trim())) {
                nonEmptyStrings.add(string.trim());
            }
        }
        return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
    }
}
