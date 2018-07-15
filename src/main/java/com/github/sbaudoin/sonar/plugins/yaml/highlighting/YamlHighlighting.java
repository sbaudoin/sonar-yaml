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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.yaml.snakeyaml.tokens.Token;
import com.github.sbaudoin.yamllint.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YamlHighlighting {
    private static final Logger LOGGER = Loggers.get(YamlHighlighting.class);
    public static final String BOM_CHAR = "\ufeff";
    private static final String YAML_DECLARATION_TAG = "---";

    private List<HighlightingData> highlighting = new ArrayList<>();

    private TypeOfText currentCode = null;

    private YamlLocation yamlFileStartLocation;
    private String content;

    public YamlHighlighting(InputFile yamlFile) throws IOException {
        this(yamlFile.contents(), String.format("Can't highlight file: %s", yamlFile.filename()));
    }

    public YamlHighlighting(String yamlStrContent) {
        this(yamlStrContent, String.format("Can't highlight code: %n%s", yamlStrContent));
    }

    private YamlHighlighting(String yamlStrContent, String errorMessage) {
        if (yamlStrContent.startsWith(BOM_CHAR)) {
            // remove it immediately
            LOGGER.debug("Document starts with BOM sequence");
            yamlStrContent = yamlStrContent.substring(1);
        }
        int realStartIndex = yamlStrContent.indexOf(YAML_DECLARATION_TAG);
        LOGGER.debug("realStartIndex = " + realStartIndex);
        try {
            // No YAML declaration tag?
            if (realStartIndex == -1) {
                yamlFileStartLocation = new YamlLocation(yamlStrContent);
                content = yamlStrContent;
            } else {
                yamlFileStartLocation = new YamlLocation(yamlStrContent).moveBefore(YAML_DECLARATION_TAG);
                content = yamlStrContent.substring(realStartIndex);
            }
            highlightYAML();
        } catch (IllegalStateException e) {
            LOGGER.warn(errorMessage, e);
        }
    }

    public List<HighlightingData> getHighlightingData() {
        return highlighting;
    }


    private void highlightYAML() {
        Iterator<Parser.Lined> items = Parser.getTokensOrComments(content).iterator();
        while (items.hasNext()) {
            Parser.Lined item = items.next();
            if (item instanceof Parser.Comment) {
                highlightComment((Parser.Comment)item);
            } else {
                highlightToken((Parser.Token)item);
            }
        }
    }

    private void highlightComment(Parser.Comment comment) {
        YamlLocation startLocation = new YamlLocation(content, comment.getLineNo(), comment.getColumnNo(), 0);
        YamlLocation endLocation = new YamlLocation(content, comment.getTokenAfter().getStartMark());

        LOGGER.trace("Highlighting comment: " + comment.toString());
        addHighlighting(startLocation, endLocation, TypeOfText.COMMENT);
    }

    private void highlightToken(Parser.Token token) {
        Token currentToken = token.getCurr();
        YamlLocation startLocation = new YamlLocation(content, currentToken.getStartMark());
        YamlLocation endLocation = new YamlLocation(content, currentToken.getEndMark());

        switch (token.getCurr().getTokenId()) {
            case DocumentStart: case DocumentEnd:
                LOGGER.trace("Highlighting document start: ---");
                addHighlighting(startLocation, endLocation, TypeOfText.CONSTANT);
                break;

            case Key:
                LOGGER.trace("Key to come");
                currentCode = TypeOfText.KEYWORD;
                break;

            case Value:
                LOGGER.trace("Value to come");
                currentCode = TypeOfText.STRING;
                break;

            case Scalar:
                LOGGER.trace("Highlighting scalar of type " + currentCode + ": " + content.substring(currentToken.getStartMark().getIndex(), currentToken.getEndMark().getIndex()));
                addHighlighting(startLocation, endLocation, currentCode);
                break;

            case Directive:
                LOGGER.trace("Highlighting directive: " + content.substring(currentToken.getStartMark().getIndex(), currentToken.getEndMark().getIndex()));
                addHighlighting(startLocation, endLocation, TypeOfText.COMMENT);
                break;

            case Anchor: case Alias:
                LOGGER.trace("Highlighting anchor or alias: " + content.substring(currentToken.getStartMark().getIndex(), currentToken.getEndMark().getIndex()));
                addHighlighting(startLocation, endLocation, TypeOfText.ANNOTATION);
                break;

            case Tag:
                LOGGER.trace("Highlighting tag: " + content.substring(currentToken.getStartMark().getIndex(), currentToken.getEndMark().getIndex()));
                addHighlighting(startLocation, endLocation, TypeOfText.PREPROCESS_DIRECTIVE);
                break;

            default:
                break;
        }
    }

    private void addHighlighting(YamlLocation start, YamlLocation end, TypeOfText typeOfText) {
        if (start.isSameAs(end)) {
            throw new IllegalArgumentException("Cannot highlight an empty range");
        }
        int startLine = start.line() + yamlFileStartLocation.line() - 1;
        int startColumn = start.column() + (start.line() == yamlFileStartLocation.line() ? (yamlFileStartLocation.column() - 1) : 0);
        int endLine = end.line() + yamlFileStartLocation.line() - 1;
        int endColumn = end.column() + (end.line() == yamlFileStartLocation.line() ? (yamlFileStartLocation.column() - 1) : 0);
        highlighting.add(new HighlightingData(startLine, startColumn, endLine, endColumn, typeOfText));
    }
}
