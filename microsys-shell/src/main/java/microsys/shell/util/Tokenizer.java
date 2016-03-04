package microsys.shell.util;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

import microsys.shell.model.Token;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * A utility class used to tokenize user input.
 */
public class Tokenizer {
    @Nonnull
    public static List<Token> tokenize(@Nonnull final String input) throws ParseException {
        Objects.requireNonNull(input);
        final List<Token> tokens = new ArrayList<>();
        boolean inQuote = false;
        boolean inEscapeSequence = false;
        String hexChars = null;
        char inQuoteChar = '"';

        final byte[] token = new byte[input.length()];
        final byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        int tokenLength = 0, i = 0;
        for (; i < input.length(); ++i) {
            final char ch = input.charAt(i);

            // if I ended up in an escape sequence, check for valid escapable character, and add it as a literal
            if (inEscapeSequence) {
                inEscapeSequence = false;
                if (ch == 'x') {
                    hexChars = "";
                } else if (Character.isWhitespace(ch) || ch == '\'' || ch == '"' || ch == '\\') {
                    token[tokenLength++] = inputBytes[i];
                } else {
                    throw new ParseException("Illegal escape sequence", i);
                }
            } else if (hexChars != null) {
                // in a hex escape sequence
                final int digit = Character.digit(ch, 16);
                if (digit < 0) {
                    throw new ParseException("Expected hex character", i);
                }
                hexChars += ch;
                if (hexChars.length() == 2) {
                    token[tokenLength++] = (byte) (0xff & Short.parseShort(hexChars, 16));
                    hexChars = null;
                }
            } else if (inQuote) {
                // in a quote, either end the quote, start escape, or continue a token
                if (ch == inQuoteChar) {
                    inQuote = false;
                    tokens.add(new Token(i - tokenLength, new String(token, 0, tokenLength, ISO_8859_1)));
                    tokenLength = 0;
                } else if (ch == '\\') {
                    inEscapeSequence = true;
                } else {
                    token[tokenLength++] = inputBytes[i];
                }
            } else {
                // not in a quote, either enter a quote, end a token, start escape, or continue a token
                if (ch == '\'' || ch == '"') {
                    if (tokenLength > 0) {
                        tokens.add(new Token(i - tokenLength, new String(token, 0, tokenLength, ISO_8859_1)));
                        tokenLength = 0;
                    }
                    inQuote = true;
                    inQuoteChar = ch;
                } else if (Character.isWhitespace(ch) && tokenLength > 0) {
                    tokens.add(new Token(i - tokenLength, new String(token, 0, tokenLength, ISO_8859_1)));
                    tokenLength = 0;
                } else if (ch == '\\') {
                    inEscapeSequence = true;
                } else if (!Character.isWhitespace(ch)) {
                    token[tokenLength++] = inputBytes[i];
                }
            }
        }
        if (inQuote) {
            throw new ParseException("Missing terminating quote", input.length());
        } else if (inEscapeSequence || hexChars != null) {
            throw new ParseException("Escape sequence not complete", input.length());
        }
        if (tokenLength > 0) {
            tokens.add(new Token(i - tokenLength, new String(token, 0, tokenLength, ISO_8859_1)));
        }
        return tokens;
    }
}
