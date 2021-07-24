/**
 * Copyright (c) 2018-2021, Sylvain Baudoin
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

import com.github.sbaudoin.sonar.plugins.yaml.checks.YamlSourceCode;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.yaml.snakeyaml.tokens.Token;
import com.github.sbaudoin.yamllint.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class in charge of YAML code highlighting in SonarQube
 */
public class YamlHighlighting {
    private static final Logger LOGGER = Loggers.get(YamlHighlighting.class);

    /**
     * The optional series of 3 bytes that mark the beginning of an UTF-8 file
     */
    public static final String BOM_CHAR = "\ufeff";


    private List<HighlightingData> highlighting = new ArrayList<>();
    private TypeOfText currentCode = TypeOfText.KEYWORD;
    private String content;


    /**
     * Constructor
     *
     * @param sourceCode the YAML source code to be highlighted
     * @throws IOException if an error occurred reading the file
     * @throws IllegalArgumentException if {@code sourceCode} is {@code null}
     */
    public YamlHighlighting(YamlSourceCode sourceCode) throws IOException {
        if (sourceCode == null) {
            throw new IllegalArgumentException("Input YAML source code cannot be null");
        }
        process(sourceCode.getContent());
    }


    /**
     * Processes the passed YAML string
     *
     * @param yamlStrContent the YAML code to be highlighted in SonarQube. Cannot be {@code null}.
     */
    private void process(String yamlStrContent) {
        if ("".equals(yamlStrContent)) {
            return;
        }

        if (yamlStrContent.startsWith(BOM_CHAR)) {
            // remove it immediately
            LOGGER.debug("Document starts with BOM sequence");
            yamlStrContent = yamlStrContent.substring(1);
        }

        content = yamlStrContent;
        highlightYAML();
    }

    /**
     * Returns the list of highlighting data found for the YAML code
     *
     * @return the list of highlighting data found for the YAML code (possibly empty but never {@code null})
     */
    public List<HighlightingData> getHighlightingData() {
        return highlighting;
    }


    /**
     * Parses the YAML code to create highlightings
     */
    private void highlightYAML() {
        for (Iterator<Parser.Lined> items = Parser.getTokensOrComments(content).iterator(); items.hasNext(); ) {
            Parser.Lined item = items.next();
            if (item instanceof Parser.Comment) {
                highlightComment((Parser.Comment)item);
            } else {
                highlightToken((Parser.Token)item);
            }
        }
    }

    /**
     * Creates an {@code HighlightingData} for a comment
     *
     * @param comment a comment to be highlighted
     */
    private void highlightComment(Parser.Comment comment) {
        YamlLocation startLocation = new YamlLocation(content, comment.getLineNo(), comment.getColumnNo(), 0);
        // We stop the highlighting right before the next token, not at the very end of the comment
        YamlLocation endLocation = new YamlLocation(content, comment.getTokenAfter().getStartMark());

        LOGGER.trace("Highlighting comment: " + comment);
        addHighlighting(startLocation, endLocation, TypeOfText.COMMENT);
    }

    /**
     * Creates an {@code HighlightingData} for a code token
     *
     * @param token a token to be highlighted
     */
    private void highlightToken(Parser.Token token) {
        Token currentToken = token.getCurr();
        YamlLocation startLocation = new YamlLocation(content, currentToken.getStartMark());
        YamlLocation endLocation = new YamlLocation(content, currentToken.getEndMark());

        switch (currentToken.getTokenId()) {
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

    /**
     * Creates an {@code HighlightingData} with the passed characteristics
     *
     * @param start the highlighting start location
     * @param end the highlighting end location
     * @param typeOfText the type of highlighted text
     */
    private void addHighlighting(YamlLocation start, YamlLocation end, TypeOfText typeOfText) {
        if (start.isSameAs(end)) {
            throw new IllegalArgumentException("Cannot highlight an empty range");
        }

        highlighting.add(new HighlightingData(start.line(), start.column(), end.line(), end.column(), typeOfText));
    }
}
