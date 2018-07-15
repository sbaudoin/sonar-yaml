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
package com.github.sbaudoin.sonar.plugins.yaml.highlighting;

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
