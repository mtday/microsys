package microsys.shell.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import microsys.shell.model.Token;

import java.text.ParseException;
import java.util.List;

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
        final List<Token> tokens = Tokenizer.tokenize("a  b");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "a"), tokens.get(0));
        assertEquals(new Token(3, "b"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithTab() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("a\tb");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "a"), tokens.get(0));
        assertEquals(new Token(2, "b"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEscapeDoubleQuote() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input \\\"");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, "\""), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEscapeSingleQuote() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input \\'");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, "'"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEscapeBackslash() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input \\\\");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, "\\"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEscapeSpace() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input a\\ b");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, "a b"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEscapedHexChar() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input \\x25");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(9, "%"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithDoubleQuotedString() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input \"quoted string\"");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, "quoted string"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithSingleQuotedString() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input 'quoted string'");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, "quoted string"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEmptyDoubleQuotedString() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input \"\"");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, ""), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEmptySingleQuotedString() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input ''");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(7, ""), tokens.get(1));
    }

    @Test
    public void testTokenizeWithDoubleQuotedStringRightNextToInput() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input\"a\"");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(6, "a"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEmptySingleQuotedStringRightNextToInput() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input'a'");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(6, "a"), tokens.get(1));
    }

    @Test
    public void testTokenizeWithEscapeInsideDoubleQuotedString() throws ParseException {
        final List<Token> tokens = Tokenizer.tokenize("input \"\\\"\"");
        assertEquals(2, tokens.size());
        assertEquals(new Token(0, "input"), tokens.get(0));
        assertEquals(new Token(8, "\""), tokens.get(1));
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
