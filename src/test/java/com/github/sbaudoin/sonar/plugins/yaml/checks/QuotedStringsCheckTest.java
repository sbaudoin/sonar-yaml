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
package com.github.sbaudoin.sonar.plugins.yaml.checks;

import com.github.sbaudoin.yamllint.YamlLintConfigException;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

public class QuotedStringsCheckTest extends TestCase {
    @SuppressWarnings("unchecked")
    public void testCheck() throws YamlLintConfigException {
        QuotedStringsCheck check = new QuotedStringsCheck();

        // Default values
        Map<String, Object> conf = (Map<String, Object>) check.getYamlLintconfig().getRuleConf("quoted-strings");
        assertEquals(true, conf.get("required"));
        assertTrue(conf.get("extra-required") instanceof List);
        assertEquals(0, ((List<?>) conf.get("extra-required")).size());
        assertTrue(conf.get("extra-allowed") instanceof List);
        assertEquals(0, ((List<?>) conf.get("extra-allowed")).size());

        // List of regexp
        check.required = "false";
        check.extraRequired = "^expr'ession[sS]?\nanother\\.exp";
        conf = (Map<String, Object>) check.getYamlLintconfig().getRuleConf("quoted-strings");
        assertTrue(conf.get("extra-required") instanceof List);
        assertEquals(2, ((List<?>) conf.get("extra-required")).size());
        assertEquals("^expr'ession[sS]?", ((List<?>) conf.get("extra-required")).get(0));
        assertEquals("another\\.exp", ((List<?>) conf.get("extra-required")).get(1));

        // List of regexp
        check.required = "only-when-needed";
        check.extraAllowed = "^expr'ession[sS]?\nanother\\.exp";
        conf = (Map<String, Object>) check.getYamlLintconfig().getRuleConf("quoted-strings");
        assertTrue(conf.get("extra-allowed") instanceof List);
        assertEquals(2, ((List<?>) conf.get("extra-allowed")).size());
        assertEquals("^expr'ession[sS]?", ((List<?>) conf.get("extra-allowed")).get(0));
        assertEquals("another\\.exp", ((List<?>) conf.get("extra-allowed")).get(1));
    }
}
