package microsys.shell.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import microsys.common.model.Model;
import microsys.common.util.CollectionComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An immutable representation of tokenized user input received from the shell interface.
 */
public class TokenizedUserInput implements Model, Comparable<TokenizedUserInput> {
    private final List<String> tokens;

    /**
     * @param userInput the original user-provided input
     */
    public TokenizedUserInput(final UserInput userInput) throws ParseException {
        this.tokens = new ArrayList<>(tokenize(userInput.getInput()));
    }

    /**
     * @param json the json from which this object will be created
     */
    public TokenizedUserInput(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("tokens"), "Tokens are required");
        Preconditions.checkArgument(json.get("tokens").isJsonArray(), "Tokens must be an array");

        this.tokens = new ArrayList<>();
        final JsonArray tokenArr = json.getAsJsonArray("tokens");
        tokenArr.forEach(e -> Preconditions.checkArgument(e.isJsonPrimitive(), "Token must be a primitive"));
        tokenArr.forEach(e -> this.tokens.add(e.getAsJsonPrimitive().getAsString()));
    }

    /**
     * @return the tokens parsed from the user-provided input from the shell interface
     */
    public List<String> getTokens() {
        return Collections.unmodifiableList(this.tokens);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonArray tokensArr = new JsonArray();
        getTokens().forEach(tokensArr::add);
        final JsonObject json = new JsonObject();
        json.add("tokens", tokensArr);
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("tokens", getTokens());
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final TokenizedUserInput other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getTokens(), other.getTokens(), new CollectionComparator<>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof TokenizedUserInput) && compareTo((TokenizedUserInput) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getTokens());
        return hash.toHashCode();
    }

    protected List<String> tokenize(final String input) throws ParseException {
        final List<String> tokens = new ArrayList<>();
        boolean inQuote = false;
        boolean inEscapeSequence = false;
        String hexChars = null;
        char inQuoteChar = '"';

        final byte[] token = new byte[input.length()];
        final byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        int tokenLength = 0;
        for (int i = 0; i < input.length(); ++i) {
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
                    tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
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
                        tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
                        tokenLength = 0;
                    }
                    inQuote = true;
                    inQuoteChar = ch;
                } else if (Character.isWhitespace(ch) && tokenLength > 0) {
                    tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
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
            tokens.add(new String(token, 0, tokenLength, StandardCharsets.ISO_8859_1));
        }
        return tokens;
    }
}
