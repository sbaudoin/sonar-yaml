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
package com.github.sbaudoin.sonar.plugins.yaml.linecounter;

import java.util.Set;

/**
 * Bean that holds a summary of the lines of a YAML file (total number of lines, actual line numbers of line of code and
 * line numbers of comments)
 */
public class LineCountData {
    private Integer linesNumber;
    private Set<Integer> linesOfCodeLines;
    private Set<Integer> effectiveCommentLine;

    /**
     * Constructor
     *
     * @param linesNumber a number of lines
     * @param linesOfCodeLines set of line numbers of line of code
     * @param effectiveCommentLine set of line numbers of comments
     */
    public LineCountData(Integer linesNumber, Set<Integer> linesOfCodeLines, Set<Integer> effectiveCommentLine) {
        this.linesNumber = linesNumber;
        this.linesOfCodeLines = linesOfCodeLines;
        this.effectiveCommentLine = effectiveCommentLine;
    }

    /**
     * Returns the total number of line of a YAML file
     *
     * @return the total number of line of a YAML file
     */
    public Integer linesNumber() {
        return linesNumber;
    }

    /**
     * Returns the set of line numbers of lines of code
     *
     * @return the set of line numbers of lines of code
     */
    public Set<Integer> linesOfCodeLines() {
        return linesOfCodeLines;
    }

    /**
     * Returns the set of line numbers of comments
     *
     * @return the set of line numbers of comments
     */
    public Set<Integer> effectiveCommentLines() {
        return effectiveCommentLine;
    }
}
