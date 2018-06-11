package com.github.sbaudoin.sonar.plugins.yaml.languages;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;
import com.github.sbaudoin.sonar.plugins.yaml.settings.YamlSettings;

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
        settings.setProperty(YamlSettings.FILE_SUFFIXES_KEY, ".myYaml");
        Assert.assertArrayEquals(new String[] { ".myYaml" }, yaml.getFileSuffixes());
    }
}
