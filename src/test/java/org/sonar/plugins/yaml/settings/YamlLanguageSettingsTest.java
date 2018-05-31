package org.sonar.plugins.yaml.settings;

import junit.framework.TestCase;
import org.sonar.api.config.PropertyDefinition;

import java.util.List;

public class YamlLanguageSettingsTest extends TestCase {
    public void testGetProperties() {
        List<PropertyDefinition> defs = YamlSettings.getProperties();

        assertEquals(1, defs.size());
        assertEquals(YamlSettings.FILE_SUFFIXES_KEY, defs.get(0).key());
        assertEquals(YamlSettings.FILE_SUFFIXES_DEFAULT_VALUE, defs.get(0).defaultValue());
    }
}
