/**
 * Copyright (c) 2018, Sylvain Baudoin
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

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class CheckRepository {
    public static final String REPOSITORY_KEY = "yaml";
    public static final String REPOSITORY_NAME = "YAML Analyzer";

    private static final List<Class> CHECK_CLASSES = Arrays.asList(
            BracesCheck.class,
            BracketsCheck.class,
            ColonsCheck.class,
            CommasCheck.class,
            CommentsCheck.class,
            CommentsIndentationCheck.class,
            DocumentEndCheck.class,
            DocumentStartCheck.class,
            EmptyLinesCheck.class,
            EmptyValuesCheck.class,
            ForbiddenKeyCheck.class,
            ForbiddenValueCheck.class,
            HyphensCheck.class,
            IndentationCheck.class,
            KeyDuplicatesCheck.class,
            KeyOrderingCheck.class,
            LineLengthCheck.class,
            NewLineAtEndOfFileCheck.class,
            NewLinesCheck.class,
            OctalValuesCheck.class,
            ParsingErrorCheck.class,
            TrailingSpacesCheck.class,
            TruthyCheck.class,
            QuotedStringsCheck.class
        );

    private static final List<String> TEMPLATE_RULE_KEYS = Arrays.asList(
            "ForbiddenKeyCheck"
    );


    /**
     * Hide constructor
     */
    private CheckRepository() {
    }


    /**
     * Returns the rule key of the check {@link ParsingErrorCheck}
     *
     * @return the rule key of the check {@link ParsingErrorCheck}
     */
    public static Class getParsingErrorCheckClass() {
        return ParsingErrorCheck.class;
    }

    /**
     * Returns all non-syntactical check classes
     *
     * @return all check classes
     */
    public static List<Class> getCheckClasses() {
        return CHECK_CLASSES;
    }

    /**
     * Returns the keys of the rules that are parameterized, i.e. that are templates
     *
     * @return the keys of the rules that are parameterized, i.e. that are templates
     */
    public static List<String> getTemplateRuleKeys() {
        return TEMPLATE_RULE_KEYS;
    }
}
