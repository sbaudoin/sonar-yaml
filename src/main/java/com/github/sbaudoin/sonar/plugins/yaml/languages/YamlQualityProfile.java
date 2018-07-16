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
package com.github.sbaudoin.sonar.plugins.yaml.languages;

import com.github.sbaudoin.sonar.plugins.yaml.checks.CheckRepository;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

/**
 * Default, built-in quality profile for the projects having YAML files
 */
public class YamlQualityProfile implements BuiltInQualityProfilesDefinition {
    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Sonar way", YamlLanguage.KEY);
        profile.setDefault(true);

        // Syntax error check
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "ParsingErrorCheck");

        // Other regular checks
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "BracesCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "BracketsCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "ColonsCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "CommasCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "CommentsCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "CommentsIndentationCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "DocumentStartCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "EmptyLinesCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "EmptyValuesCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "HyphensCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "IndentationCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "KeyDuplicatesCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "LineLengthCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "NewLineAtEndOfFileCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "NewLinesCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "OctalValuesCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "TrailingSpacesCheck");
        profile.activateRule(CheckRepository.REPOSITORY_KEY, "TruthyCheck");

        profile.done();
    }
}
