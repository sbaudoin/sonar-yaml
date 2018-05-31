package org.sonar.plugins.yaml.highlighting;

import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;

public class HighlightingData {
    private final TypeOfText typeOfText;

    private int startLine;
    private int startColumnOffset;
    private int endLine;
    private int endColumnOffset;

    public HighlightingData(int startLine, int startColumnIndex, int endLine, int endColumnIndex, TypeOfText typeOfText) {
        this.startLine = startLine;
        this.startColumnOffset = startColumnIndex - 1;
        this.endLine = endLine;
        this.endColumnOffset = endColumnIndex - 1;
        this.typeOfText = typeOfText;
    }

    public int startLine() {
        return startLine;
    }

    public int startColumn() {
        return startColumnOffset;
    }

    public int endLine() {
        return endLine;
    }

    public int endColumn() {
        return endColumnOffset;
    }

    public TypeOfText highlightCode() {
        return typeOfText;
    }

    public void highlight(NewHighlighting highlighting) {
        highlighting.highlight(startLine, startColumnOffset, endLine, endColumnOffset, typeOfText);
    }
}
