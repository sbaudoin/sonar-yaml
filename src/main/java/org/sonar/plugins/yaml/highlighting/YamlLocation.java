package org.sonar.plugins.yaml.highlighting;

import org.yaml.snakeyaml.error.Mark;

public class YamlLocation {
    private final String content;
    private final int line;
    private final int column;
    private final int characterOffset;

    YamlLocation(String content) {
        // based on YAML parser:
        // - lines start at 1
        // - columns start at at 1
        // - offset start at at 0
        this(content, 1, 1, 0);
    }

    YamlLocation(String content, Mark mark) {
        this(content, mark.getLine() + 1, mark.getColumn() + 1, mark.getPointer());
    }

    public YamlLocation(String content, int line, int column, int characterOfffset) {
        this.content = content;
        this.line = line;
        this.column = column;
        this.characterOffset = characterOfffset;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");
        sb.append("content: \"").append(content).append("\"; ");
        sb.append("line: ").append(line).append("; ");
        sb.append("column: ").append(column).append("; ");
        sb.append("characterOffset: ").append(characterOffset);
        sb.append(" }");

        return sb.toString();
    }

    public YamlLocation shift(int nbChar) {
        if (characterOffset + nbChar > content.length()) {
            throw new IllegalStateException("Cannot shift by " + nbChar + "characters");
        }
        YamlLocation res = this;
        for (int i = 0; i < nbChar; i++) {
            res = res.shift(res.readChar());
        }
        return res;
    }

    public YamlLocation moveBackward() {
        if (column == 1) {
            throw new IllegalStateException("Cannot move backward from column 1");
        }
        return new YamlLocation(content, line, column - 1, characterOffset - 1);
    }

    public char readChar() {
        return content.charAt(characterOffset);
    }

    private YamlLocation shift(char c) {
        if (c == '\n') {
            return new YamlLocation(content, line + 1, 1, characterOffset + 1);
        }
        return new YamlLocation(content, line, column + 1, characterOffset + 1);
    }

    public boolean startsWith(String prefix) {
        return content.substring(characterOffset).startsWith(prefix);
    }

    public YamlLocation moveAfter(String substring) {
        return moveBefore(substring).shift(substring.length());
    }

    public YamlLocation moveBefore(String substring) {
        int index = content.substring(characterOffset).indexOf(substring);
        if (index == -1) {
            throw new IllegalStateException("Cannot find " + substring + " in " + content.substring(characterOffset));
        }
        return shift(index);
    }

    public boolean isSameAs(YamlLocation other) {
        return this.characterOffset == other.characterOffset;
    }

    public YamlLocation moveAfterWhitespaces() {
        YamlLocation res = this;
        while (Character.isWhitespace(res.readChar())) {
            res = res.shift(1);
        }
        return res;
    }

    public boolean has(String substring, YamlLocation max) {
        YamlLocation location = this;
        while (location.characterOffset < max.characterOffset) {
            if (location.startsWith(substring)) {
                return true;
            }
            location = location.shift(1);
        }
        return false;
    }
}
