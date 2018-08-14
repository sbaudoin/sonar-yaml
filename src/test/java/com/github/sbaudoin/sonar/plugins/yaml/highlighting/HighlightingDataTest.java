package com.github.sbaudoin.sonar.plugins.yaml.highlighting;

import junit.framework.TestCase;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.highlighting.NewHighlighting;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;

public class HighlightingDataTest extends TestCase {
    public void test() {
        NewHighlightingTester highlighting = new NewHighlightingTester();

        HighlightingData hd = new HighlightingData(1, 2, 3, 4, TypeOfText.COMMENT);
        hd.highlight(highlighting);
        assertEquals(1, hd.getStartLine());
        assertEquals(1, highlighting.getStartLine());
        assertEquals(2, hd.getStartColumnIndex());
        assertEquals(1, highlighting.getStartLineOffset());
        assertEquals(3, hd.getEndLine());
        assertEquals(3, highlighting.getEndLine());
        assertEquals(4, hd.getEndColumnIndex());
        assertEquals(3, highlighting.getEndLineOffset());
        assertEquals(TypeOfText.COMMENT, hd.getTypeOfText());
        assertEquals(TypeOfText.COMMENT, highlighting.getTypeOfText());
    }

    private class NewHighlightingTester implements NewHighlighting {
        private int startLine;
        private int startLineOffset;
        private int endLine;
        private int endLineOffset;
        private TypeOfText typeOfText;

        @Override
        public NewHighlighting onFile(InputFile inputFile) {
            return null;
        }

        @Override
        public NewHighlighting highlight(int startOffset, int endOffset, TypeOfText typeOfText) {
            return null;
        }

        @Override
        public NewHighlighting highlight(TextRange range, TypeOfText typeOfText) {
            return null;
        }

        @Override
        public NewHighlighting highlight(int startLine, int startLineOffset, int endLine, int endLineOffset, TypeOfText typeOfText) {
            this.startLine = startLine;
            this.startLineOffset = startLineOffset;
            this.endLine = endLine;
            this.endLineOffset = endLineOffset;
            this.typeOfText = typeOfText;
            return this;
        }

        @Override
        public void save() {
        }

        public int getEndLine() {
            return endLine;
        }

        public int getEndLineOffset() {
            return endLineOffset;
        }

        public int getStartLine() {
            return startLine;
        }

        public int getStartLineOffset() {
            return startLineOffset;
        }

        public TypeOfText getTypeOfText() {
            return typeOfText;
        }
    }
}
