/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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

@Rule(key = "QuotedStringsCheck")
public class QuotedStringsCheck extends YamlLintCheck {
    private static final Logger LOGGER = Loggers.get(QuotedStringsCheck.class);


    @RuleProperty(key = "quote-type", description = "Indicates the expected quote type: single ('), double (\") or any (default)", defaultValue = "any")
    String quoteType = "any";

    @RuleProperty(key = "required", description = "Defines whether using quotes in string values is required (\"true\", default) or not (\"false\"), or only allowed when really needed (\"only-when-needed\")", defaultValue = "true")
    String required = "true";

    @RuleProperty(key = "extra-required", description = "List of regexes to force string values to be quoted, if they match any regex. This option can only be used with required=\"false\" and required=\"only-when-needed\". One regex per line.", defaultValue = "", type="TEXT")
    String extraRequired = "";

    @RuleProperty(key = "extra-allowed", description = "List of regexes to allow quoted string values, even if required=\"only-when-needed\" is set. One regex per line.", defaultValue = "", type="TEXT")
    String extraAllowed = "";


    @Override
    protected YamlLintConfig getYamlLintconfig() throws YamlLintConfigException {
        Yaml yaml = new Yaml();
        StringBuilder propsSB = new StringBuilder();
        for (Field f : getClass().getDeclaredFields()) {
            RuleProperty rp = f.getAnnotation(RuleProperty.class);
            LOGGER.debug("Got RuleProperty " + rp);
            if (rp != null) {
                try {
                    if ("extra-required".equals(rp.key()) || "extra-allowed".equals(rp.key())) {
                        if (f.get(this) != null && !"".equals(f.get(this))) {
                            propsSB.append("    ").append(rp.key()).append(": ").append(yaml.dump(((String) f.get(this)).split("\n")));
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
