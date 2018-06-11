package com.github.sbaudoin.sonar.plugins.yaml.linecounter;

import org.apache.commons.lang.StringUtils;
import com.github.sbaudoin.yamllint.Parser;

import java.util.*;

/**
 * Counting comment lines, blank lines in YAML files
 */
public final class LineCountParser {
    private int linesNumber;
    private Set<Integer> commentLines;
    private Set<Integer> linesOfCodeLines;
    private LineCountData data;


    public LineCountParser(String contents) {
        this.commentLines = new HashSet<>();
        this.linesOfCodeLines = new HashSet<>();
        linesNumber = 0;

        List<Parser.Line> lines = Parser.getLines(contents);

        for (Parser.Line line : lines) {
            String lineContent = line.getContent();
            if (isCommentLine(lineContent)) {
                commentLines.add(line.getLineNo());
            } else if (!StringUtils.isBlank(lineContent)) {
                linesOfCodeLines.add(line.getLineNo());
            }

            if (line.getLineNo() > linesNumber) {
                linesNumber = line.getLineNo();
            }
        }

        this.data = new LineCountData(
                linesNumber,
                linesOfCodeLines,
                commentLines);
    }

    public LineCountData getLineCountData() {
        return data;
    }


    /**
     * Tells if the passed line is a comment line, i.e. a line with only a comment
     *
     * @param lineContent
     * @return
     */
    private boolean isCommentLine(String lineContent) {
        assert lineContent != null;

        return lineContent.matches("^\\s*#\\s*\\S.*");
    }
}
