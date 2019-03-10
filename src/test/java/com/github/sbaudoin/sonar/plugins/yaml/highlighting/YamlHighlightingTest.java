package com.github.sbaudoin.sonar.plugins.yaml.highlighting;

import com.github.sbaudoin.sonar.plugins.yaml.Utils;
import com.github.sbaudoin.sonar.plugins.yaml.checks.YamlSourceCode;
import org.junit.Rule;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.api.utils.log.LogTester;
import org.sonar.api.utils.log.LoggerLevel;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class YamlHighlightingTest {
    @Rule
    public LogTester logTester = new LogTester();

    @Test
    public void testConstructors() throws IOException {
        try {
            new YamlHighlighting(null);
            fail("Null values should not be accepted");
        } catch (IllegalArgumentException e) {
            assertEquals("Input YAML source code cannot be null", e.getMessage());
        }

        YamlHighlighting yh = new YamlHighlighting(new YamlSourceCode(Utils.getInputFile("dummy-file.yaml"), Optional.of(false)));
        assertEquals(6, yh.getHighlightingData().size());
    }

    @Test
    public void testBOM() throws IOException {
        YamlHighlighting yh = new YamlHighlighting(getSourceCode("\ufeff---\nkey: value\n"));

        assertTrue(logTester.logs(LoggerLevel.DEBUG).contains("Document starts with BOM sequence"));
        assertEquals(3, yh.getHighlightingData().size());
    }

    @Test
    public void testBrokenYaml() throws IOException {
        YamlHighlighting yh = new YamlHighlighting(getSourceCode("not: a: valid: yaml\n# Even with a comment"));
        // Only the first token is expected to be highlighted
        assertEquals(1, yh.getHighlightingData().size());
        assertHighlightingData(yh.getHighlightingData().get(0), 1, 1, 1, 4, TypeOfText.KEYWORD);

        yh = new YamlHighlighting(getSourceCode("{{- if .Values.hpa.enabled -}}\n"));
        // Only the first token is expected to be highlighted with default type
        assertEquals(1, yh.getHighlightingData().size());
        assertHighlightingData(yh.getHighlightingData().get(0), 1, 5, 1, 29, TypeOfText.KEYWORD);
    }

    @Test
    public void testHighlightWithStart() throws IOException {
        YamlHighlighting yh = new YamlHighlighting(getSourceCode("# Comment then document start mark\n---\nkey: value"));

        assertEquals(4, yh.getHighlightingData().size());
        assertHighlightingData(yh.getHighlightingData().get(0), 1, 1, 2, 1, TypeOfText.COMMENT);
        assertHighlightingData(yh.getHighlightingData().get(1), 2, 1, 2, 4, TypeOfText.CONSTANT);
        assertHighlightingData(yh.getHighlightingData().get(2), 3, 1, 3, 4, TypeOfText.KEYWORD);
        assertHighlightingData(yh.getHighlightingData().get(3), 3, 6, 3, 11, TypeOfText.STRING);
    }

    @Test
    public void testHighlightNoStart() throws IOException {
        YamlHighlighting yh = new YamlHighlighting(getSourceCode("# Comment without document start mark\nkey: value"));

        assertEquals(3, yh.getHighlightingData().size());
        // Comment highlighting ends at the beginning of the next token
        assertHighlightingData(yh.getHighlightingData().get(0), 1, 1, 2, 1, TypeOfText.COMMENT);
        assertHighlightingData(yh.getHighlightingData().get(1), 2, 1, 2, 4, TypeOfText.KEYWORD);
        assertHighlightingData(yh.getHighlightingData().get(2), 2, 6, 2, 11, TypeOfText.STRING);
    }

    @Test
    public void testHighlightAllTokenTypes() throws IOException {
        YamlHighlighting yh = new YamlHighlighting(getSourceCode("%YAML 1.1\n" +
                "---\n" +
                "# Comment line\n" +
                "key1: value\n" +
                "key2:  # Inline comment\n" +
                "  - subkey1: !!str 2018\n" +
                "  - subkey2: &anchor value\n" +
                "  - subkey3: *anchor\n" +
                "...\n"));

        assertEquals(16, yh.getHighlightingData().size());
        // Directive
        assertHighlightingData(yh.getHighlightingData().get(0), 1, 1, 1, 10, TypeOfText.COMMENT);
        // Document start
        assertHighlightingData(yh.getHighlightingData().get(1), 2, 1, 2, 4, TypeOfText.CONSTANT);
        // Comment line
        assertHighlightingData(yh.getHighlightingData().get(2), 3, 1, 4, 1, TypeOfText.COMMENT);
        // Key
        assertHighlightingData(yh.getHighlightingData().get(3), 4, 1, 4, 5, TypeOfText.KEYWORD);
        // Value
        assertHighlightingData(yh.getHighlightingData().get(4), 4, 7, 4, 12, TypeOfText.STRING);
        // Another key
        assertHighlightingData(yh.getHighlightingData().get(5), 5, 1, 5, 5, TypeOfText.KEYWORD);
        // Inline comment
        assertHighlightingData(yh.getHighlightingData().get(6), 5, 8, 6, 3, TypeOfText.COMMENT);
        // Tag
        assertHighlightingData(yh.getHighlightingData().get(7), 6, 5, 6, 12, TypeOfText.KEYWORD);
        assertHighlightingData(yh.getHighlightingData().get(8), 6, 14, 6, 19, TypeOfText.PREPROCESS_DIRECTIVE);
        assertHighlightingData(yh.getHighlightingData().get(9), 6, 20, 6, 24, TypeOfText.STRING);
        // Anchor
        assertHighlightingData(yh.getHighlightingData().get(10), 7, 5, 7, 12, TypeOfText.KEYWORD);
        assertHighlightingData(yh.getHighlightingData().get(11), 7, 14, 7, 21, TypeOfText.ANNOTATION);
        assertHighlightingData(yh.getHighlightingData().get(12), 7, 22, 7, 27, TypeOfText.STRING);
        // Alias
        assertHighlightingData(yh.getHighlightingData().get(13), 8, 5, 8, 12, TypeOfText.KEYWORD);
        assertHighlightingData(yh.getHighlightingData().get(14), 8, 14, 8, 21, TypeOfText.ANNOTATION);
        // Document end
        assertHighlightingData(yh.getHighlightingData().get(15), 9, 1, 9, 4, TypeOfText.CONSTANT);
    }

    private void assertHighlightingData(HighlightingData hd, int startLine, int startColumnIndex, int endLine, int endColumnIndex, TypeOfText typeOfText) {
        assertEquals(startLine, hd.getStartLine());
        assertEquals(startColumnIndex, hd.getStartColumnIndex());
        assertEquals(endLine, hd.getEndLine());
        assertEquals(endColumnIndex, hd.getEndColumnIndex());
        assertEquals(typeOfText, hd.getTypeOfText());
    }

    private YamlSourceCode getSourceCode(String code) throws IOException {
        YamlSourceCode sourceCode = new YamlSourceCode(Utils.getInputFile("dummy-file.yaml"), Optional.of(false));
        YamlSourceCode spy = spy(sourceCode);
        when(spy.getContent()).thenReturn(code);
        return spy;
    }
}
