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

    private static class NewHighlightingTester implements NewHighlighting {
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
