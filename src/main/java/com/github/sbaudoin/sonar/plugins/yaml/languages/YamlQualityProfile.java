package com.github.sbaudoin.sonar.plugins.yaml.languages;

import com.github.sbaudoin.sonar.plugins.yaml.checks.CheckRepository;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

/**
 * Default, BuiltIn Quality Profile for the projects having YAML files
 */
public class YamlQualityProfile implements BuiltInQualityProfilesDefinition {
    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("Sonar way", YamlLanguage.KEY);
        profile.setDefault(true);

        profile.activateRule(CheckRepository.REPOSITORY_KEY, "ParsingErrorCheck");

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
