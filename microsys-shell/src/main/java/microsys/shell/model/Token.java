package microsys.shell.model;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing a token parsed from the user input.
 */
public class Token implements Comparable<Token> {
    private final int position;
    @Nonnull
    private final String value;

    /**
     * @param position the position in the original input where this token resides
     * @param value the text value of the token
     */
    public Token(final int position, @Nonnull final String value) {
        Preconditions.checkArgument(position >= 0, "Position must be non-negative");
        this.position = position;
        this.value = Objects.requireNonNull(value);
    }

    /**
     * @return the position in the original input where this token resides
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * @return the text value of the token
     */
    @Nonnull
    public String getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        return getValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final Token other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getPosition(), other.getPosition());
        cmp.append(getValue(), other.getValue());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof Token) && compareTo((Token) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getPosition());
        hash.append(getValue());
        return hash.toHashCode();
    }
}
