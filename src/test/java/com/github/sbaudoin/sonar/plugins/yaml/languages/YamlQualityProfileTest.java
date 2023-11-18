/**
 * Copyright (c) 2018-2023, Sylvain Baudoin
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

import org.junit.jupiter.api.Test;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import static org.junit.jupiter.api.Assertions.*;

class YamlQualityProfileTest {
    @Test
    void testDefineWithoutYamlBuiltinSupport() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
        YamlQualityProfile qp = new YamlQualityProfile(false);
        qp.define(context);
        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("yaml", "YAML Analyzer");
        assertNotNull(profile);
        assertTrue(profile.isDefault());
        assertEquals(19, profile.rules().size());
    }

    @Test
    void testDefineWithYamlBuiltinSupport() {
        BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
        YamlQualityProfile qp = new YamlQualityProfile(true);
        qp.define(context);
        BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("yaml", "YAML Analyzer");
        assertNotNull(profile);
        assertFalse(profile.isDefault());
        assertEquals(19, profile.rules().size());
    }
}
