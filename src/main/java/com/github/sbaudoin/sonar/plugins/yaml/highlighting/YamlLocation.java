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

import org.yaml.snakeyaml.error.Mark;

/**
 * Wrapper class used to locate tokens in YAML code and help highlight it
 */
public class YamlLocation {
    private final String content;
    private final int line;
    private final int column;
    private final int characterOffset;


    /**
     * Constructor
     *
     * @param content the YAML content to highlight
     */
    YamlLocation(String content) {
        // based on YAML parser:
        // - lines start at 1
        // - columns start at at 1
        // - offset start at at 0
        this(content, 1, 1, 0);
    }

    /**
     * Constructor
     *
     * @param content the YAML content to highlight
     * @param mark a mark to be pointed to (and certainly highlighted) in the YAML code
     */
    YamlLocation(String content, Mark mark) {
        this(content, mark.getLine() + 1, mark.getColumn() + 1, mark.getPointer());
    }

    /**
     * Xonstrctor
     *
     * @param content the YAML content to highlight
     * @param line a line to point to (by convention, lines start at 1)
     * @param column a column to point to (by convention, columns start at 1)
     * @param characterOffset a character offset in the content equals to the number of characters read so far between
     *                        the content's start and the given column of the given line
     */
    public YamlLocation(String content, int line, int column, int characterOffset) {
        this.content = content;
        this.line = line;
        this.column = column;
        this.characterOffset = characterOffset;
    }

    /**
     * Returns the line pointer remembered by this class
     *
     * @return the line pointer remembered by this class
     */
    public int line() {
        return line;
    }

    /**
     * Returns the column pointer remembered by this class
     *
     * @return the column pointer remembered by this class
     */
    public int column() {
        return column;
    }

    /**
     * Returns a string representation of this class
     *
     * @return a string representation of this class
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");
        sb.append("content: \"").append(content).append("\"; ");
        sb.append("line: ").append(line).append("; ");
        sb.append("column: ").append(column).append("; ");
        sb.append("characterOffset: ").append(characterOffset);
        sb.append(" }");

        return sb.toString();
    }

    /**
     * Returns a {@code YamlLocation} that corresponds to this instance + line, column and offset parameters moved
     * froward of the passed number of characters
     *
     * @param nbChar a number of character to move forward
     * @return a {@code YamlLocation} updated with the passed additional offset
     * @throws IllegalStateException if the passed number of character added to the offset will point to a character
     * beyond the content's end (i.e. if {@code offset + nbChar > content.length()})
     * @see #shift(char)
     */
    public YamlLocation shift(int nbChar) {
        if (characterOffset + nbChar > content.length()) {
            throw new IllegalStateException("Cannot shift by " + nbChar + " characters");
        }
        YamlLocation res = this;
        for (int i = 0; i < nbChar; i++) {
            res = res.shift(res.readChar());
        }
        return res;
    }

    /**
     * Returns the character of the YAML content pointed to by the offset
     *
     * @return a character
     */
    public char readChar() {
        return content.charAt(characterOffset);
    }

    /**
     * Shifts the internal pointers (line, column and offset) of one character. The column and line pointers are
     * updated depended on the fact that the passed character is a new line character or not (the passed character
     * is supposed to be the character currently pointed to by the offset)
     *
     * @param c a character
     * @return a new {@code YamlLocation} moved forward of 1 character
     * @throws IllegalStateException if the passed number of character added to the offset will point to a character
     * beyond the content's end (i.e. if {@code offset + nbChar > content.length()})
     * @see #shift(int)
     */
    private YamlLocation shift(char c) {
        if (c == '\n') {
            return new YamlLocation(content, line + 1, 1, characterOffset + 1);
        }
        return new YamlLocation(content, line, column + 1, characterOffset + 1);
    }

    /**
     * Moves the internal pointers (line, column and offset) to the first character of the next coming occurrence of the
     * passed string in the YAML content
     *
     * @param substring a string to move forward to
     * @return a new {@code YamlLocation} for updated internal pointers
     * @throws IllegalStateException if the passed string cannot be found
     */
    public YamlLocation moveBefore(String substring) {
        int index = content.indexOf(substring, characterOffset);
        if (index == -1) {
            throw new IllegalStateException("Cannot find " + substring + " in " + content.substring(characterOffset));
        }
        return shift(index);
    }

    /**
     * Tells if the passed {@code YamlLocation} has the same offset or not (this is a kind of shallow version of the
     * {@code equals} method)
     *
     * @param other another {@code YamlLocation}
     * @return {@code true} if the offset of the passed {@code YamlLocation} equals to the offset of this class,
     * {@code false} if not
     */
    public boolean isSameAs(YamlLocation other) {
        return this.characterOffset == other.characterOffset;
    }
}
