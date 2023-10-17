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
package com.github.sbaudoin.sonar.plugins.yaml.checks;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import com.github.sbaudoin.yamllint.LintProblem;
import com.github.sbaudoin.yamllint.Linter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Wrapper class to ease the working with files and associated issues
 */
public class YamlSourceCode {
    private static final Logger LOGGER = Loggers.get(YamlSourceCode.class);


    private final List<YamlIssue> yamlIssues = new ArrayList<>();

    private YamlIssue syntaxError = null;
    private boolean filter;
    private InputFile yamlFile;
    private String content = null;


    /**
     * Constructor. Parses the passed file to determine if it is syntactically correct.
     *
     * @param yamlFile a supposedly YAML file
     * @param filter {@code true} to filter out UTF-8 line break characters (U+2028, U+2029 and U+0085) that may not be
     *               correctly supported by SonarQube
     * @throws IOException if there is a problem reading the passed file
     */
    public YamlSourceCode(InputFile yamlFile, Optional<Boolean> filter) throws IOException {
        this.yamlFile = yamlFile;
        this.filter = filter.orElse(false);

        LintProblem problem = Linter.getSyntaxError(getContent());
        LOGGER.debug("File {} has syntax error? {}", yamlFile.uri(), problem != null);
        if (problem != null) {
            syntaxError = new YamlLintIssue(problem, null, true);
        }
    }


    /**
     * Returns the {@code InputFile} of this class.
     * <p><strong>WARNING!!!</strong> Do not use {@code getYamlFile.contents()} to get the source; use {@link #getContent()}
     * instead.</p>
     *
     * @return the {@code InputFile} of this class
     * @see InputFile
     * @see #getContent()
     */
    public InputFile getYamlFile() {
        return yamlFile;
    }

    /**
     * Returns the content of the YAML file as a {@code String} with UTF-8 line breaks possibly removed.
     * <p><strong>WARNING!!</strong> Use this method instead of {@code InputFile.contents()} in order to have the source
     * code to be cleanup if needed.</p>
     *
     * @return the YAML content
     * @throws IOException if an error occurred reading the YAML file
     * @see #YamlSourceCode(InputFile, Optional)
     */
    public String getContent() throws IOException {
        if (content == null) {
            if (filter) {
                this.content = yamlFile.contents().replace("\u0085", "").replace("\u2028", "").replace("\u2029", "");
            } else {
                this.content = yamlFile.contents();
            }
        }

        return content;
    }

    /**
     * Adds an issue to list of issues already discovered
     *
     * @param issue an issue that relates to this YAML source code
     */
    public void addViolation(YamlIssue issue) {
        this.yamlIssues.add(issue);
        if (issue.isSyntaxError() && syntaxError == null) {
            syntaxError = issue;
        }
    }

    /**
     * Returns the syntax error if any found. May be {@code null}.
     * <p><strong>Warning!!!</strong> This issue has no rule key. It is up to the caller of this method to deal with the
     * rule key if it is required.</p>
     *
     * @return the syntax error if any
     * @see #hasCorrectSyntax()
     */
    public YamlIssue getSyntaxError() {
        return syntaxError;
    }
    /**
     * Returns {@code true} if succeeded or {@code false} if the file is corrupted (i.e. it contains
     * a syntax error you can get with {@link #getSyntaxError()})
     *
     * @return {@code true} if succeeded or {@code false} if the file is corrupted
     */
    public boolean hasCorrectSyntax() {
        return syntaxError == null;
    }

    /**
     * Returns the issues found on the source code
     *
     * @return the list of issues found on the source code. The returned list may be empty, whether because no issue has
     * been found, or the source code has not been parsed yet
     */
    public List<YamlIssue> getYamlIssues() {
        return yamlIssues;
    }
}
