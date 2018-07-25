package com.github.sbaudoin.sonar.plugins.yaml.languages;

import junit.framework.TestCase;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class YamlQualityProfileTest extends TestCase {
    public void testDefine() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
        YamlQualityProfile qp = new YamlQualityProfile();
        qp.define(context);
        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("yaml", "Sonar way");
        assertNotNull(profile);
        assertTrue(profile.isDefault());
        assertEquals(19, profile.rules().size());
    }
}
