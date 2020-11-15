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

public class TruthyCheckTest extends TestCase {
    @SuppressWarnings("unchecked")
    public void testCheck() throws YamlLintConfigException {
        TruthyCheck check = new TruthyCheck();

        // Default values
        Map<String, Object> conf = (Map<String, Object>) check.getYamlLintconfig().getRuleConf("truthy");
        assertTrue((boolean) conf.get("check-keys"));
        assertTrue(conf.get("allowed-values") instanceof List);
        assertEquals(2, ((List<?>) conf.get("allowed-values")).size());

        // List of regexp
        check.allowedValues = "a, b , c,false";
        conf = (Map<String, Object>) check.getYamlLintconfig().getRuleConf("truthy");
        assertTrue(conf.get("allowed-values") instanceof List);
        assertEquals(4, ((List<?>) conf.get("allowed-values")).size());
        assertEquals("a", ((List<?>) conf.get("allowed-values")).get(0));
        assertEquals("b", ((List<?>) conf.get("allowed-values")).get(1));
        assertEquals("c", ((List<?>) conf.get("allowed-values")).get(2));
        assertEquals("false", ((List<?>) conf.get("allowed-values")).get(3));
    }
}
