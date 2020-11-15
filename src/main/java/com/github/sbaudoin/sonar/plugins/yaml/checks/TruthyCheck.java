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

import com.github.sbaudoin.yamllint.YamlLintConfig;
import com.github.sbaudoin.yamllint.YamlLintConfigException;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Field;

@Rule(key = "TruthyCheck")
public class TruthyCheck extends YamlLintCheck {
    private static final Logger LOGGER = Loggers.get(TruthyCheck.class);


    @RuleProperty(key = "allowed-values", description = "Comma-separated list of truthy values which will be ignored during linting", defaultValue = "true, false")
    String allowedValues = "true, false";

    @RuleProperty(key = "check-keys", description = "Disable verification for keys in mappings. By default, truthy rule applies to both keys and values. Set this option to false to prevent this.", defaultValue = "true")
    boolean checkKeys = true;


    @Override
    protected YamlLintConfig getYamlLintconfig() throws YamlLintConfigException {
        Yaml yaml = new Yaml();
        StringBuilder propsSB = new StringBuilder();
        for (Field f : getClass().getDeclaredFields()) {
            RuleProperty rp = f.getAnnotation(RuleProperty.class);
            LOGGER.debug("Got RuleProperty " + rp);
            if (rp != null) {
                try {
                    if ("allowed-values".equals(rp.key())) {
                        if (f.get(this) != null && !"".equals(f.get(this))) {
                            propsSB.append("    ").append(rp.key()).append(": ").append(yaml.dump(((String) f.get(this)).split(" ?, ?")));
                        } else {
                            propsSB.append("    ").append(rp.key()).append(": []\n");
                        }
                    } else {
                        propsSB.append("    ").append(rp.key()).append(": ").append(f.get(this)).append("\n");
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Cannot get field value for '" + f.getName() + "'", e);
                    return null;
                }
            }
        }

        return getYamlLintconfig(propsSB);
    }
}
