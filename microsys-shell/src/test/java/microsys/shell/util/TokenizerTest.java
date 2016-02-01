package microsys.shell.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.text.ParseException;

/**
 * Perform testing of the {@link Tokenizer} class.
 */
public class TokenizerTest {
    @Test
    public void testConstructor() {
        // just for 100% coverage.
        new Tokenizer();
    }

    @Test
    public void testTokenizeWithMultipleSpaces() throws ParseException {
        assertEquals("[a, b]", Tokenizer.tokenize("a  b").toString());
    }

    @Test
    public void testTokenizeWithTab() throws ParseException {
        assertEquals("[a, b]", Tokenizer.tokenize("a\tb").toString());
    }

    @Test
    public void testTokenizeWithEscapeDoubleQuote() throws ParseException {
        assertEquals("[input, \"]", Tokenizer.tokenize("input \\\"").toString());
    }

    @Test
    public void testTokenizeWithEscapeSingleQuote() throws ParseException {
        assertEquals("[input, ']", Tokenizer.tokenize("input \\'").toString());
    }

    @Test
    public void testTokenizeWithEscapeBackslash() throws ParseException {
        assertEquals("[input, \\]", Tokenizer.tokenize("input \\\\").toString());
    }

    @Test
    public void testTokenizeWithEscapeSpace() throws ParseException {
        assertEquals("[input, a b]", Tokenizer.tokenize("input a\\ b").toString());
    }

    @Test
    public void testTokenizeWithEscapedHexChar() throws ParseException {
        assertEquals("[input, %]", Tokenizer.tokenize("input \\x25").toString());
    }

    @Test
    public void testTokenizeWithDoubleQuotedString() throws ParseException {
        assertEquals("[input, quoted string]", Tokenizer.tokenize("input \"quoted string\"").toString());
    }

    @Test
    public void testTokenizeWithSingleQuotedString() throws ParseException {
        assertEquals("[input, quoted string]", Tokenizer.tokenize("input 'quoted string'").toString());
    }

    @Test
    public void testTokenizeWithEmptyDoubleQuotedString() throws ParseException {
        assertEquals("[input, ]", Tokenizer.tokenize("input \"\"").toString());
    }

    @Test
    public void testTokenizeWithEmptySingleQuotedString() throws ParseException {
        assertEquals("[input, ]", Tokenizer.tokenize("input ''").toString());
    }

    @Test
    public void testTokenizeWithDoubleQuotedStringRightNextToInput() throws ParseException {
        assertEquals("[input, a]", Tokenizer.tokenize("input\"a\"").toString());
    }

    @Test
    public void testTokenizeWithEmptySingleQuotedStringRightNextToInput() throws ParseException {
        assertEquals("[input, a]", Tokenizer.tokenize("input'a'").toString());
    }

    @Test
    public void testTokenizeWithEscapeInsideDoubleQuotedString() throws ParseException {
        assertEquals("[input, \"]", Tokenizer.tokenize("input \"\\\"\"").toString());
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIllegalEscapeSequence() throws ParseException {
        Tokenizer.tokenize("input \\-");
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIncompleteEscapeSequence() throws ParseException {
        Tokenizer.tokenize("input \\");
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIncompleteEscapedHex() throws ParseException {
        Tokenizer.tokenize("input \\x0");
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithIllegalEscapedHex() throws ParseException {
        Tokenizer.tokenize("input \\xtv");
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithUnmatchedDoubleQuote() throws ParseException {
        Tokenizer.tokenize("input \"quoted string");
    }

    @Test(expected = ParseException.class)
    public void testTokenizeWithUnmatchedSingleQuote() throws ParseException {
        Tokenizer.tokenize("input 'quoted string");
    }
}
