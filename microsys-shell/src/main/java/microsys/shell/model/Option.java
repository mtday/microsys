package microsys.shell.model;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jline.console.completer.Completer;
import microsys.common.util.OptionalComparator;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing a possible option available to a command.
 */
public class Option implements Comparable<Option> {
    @Nonnull
    private final String description;
    @Nonnull
    private final String shortOption;
    @Nonnull
    private final Optional<String> longOption;
    @Nonnull
    private final Optional<String> argName;
    private final int arguments;
    private final boolean required;
    private final boolean optionalArg;
    @Nonnull
    private final Optional<Completer> completer;

    /**
     * @param description the description of the option
     * @param shortOption the name of the option in short form
     * @param longOption  the name of the option in long form, possibly empty
     * @param argName     the name of the argument for the option, possibly empty
     * @param arguments   the number of arguments expected with this option
     * @param required    whether this option is required
     * @param optionalArg whether this option supports an optional argument
     * @param completer   the completer to use when tab-completing this option, possibly empty
     */
    public Option(
            @Nonnull final String description, @Nonnull final String shortOption,
            @Nonnull final Optional<String> longOption, @Nonnull final Optional<String> argName, final int arguments,
            final boolean required, final boolean optionalArg, @Nonnull final Optional<Completer> completer) {
        Objects.requireNonNull(description);
        Preconditions.checkArgument(StringUtils.isNotEmpty(description), "Description cannot be empty");
        Objects.requireNonNull(shortOption);
        Preconditions.checkArgument(StringUtils.isNotEmpty(shortOption), "Short option cannot be empty");
        Preconditions.checkArgument(arguments >= 0, "Arguments must be non-negative");

        this.description = description;
        this.shortOption = shortOption;
        this.longOption = Objects.requireNonNull(longOption);
        this.argName = Objects.requireNonNull(argName);
        this.arguments = arguments;
        this.required = required;
        this.optionalArg = optionalArg;
        this.completer = Objects.requireNonNull(completer);
    }

    /**
     * @return the description of the option
     */
    @Nonnull
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the name of the option in short form
     */
    @Nonnull
    public String getShortOption() {
        return this.shortOption;
    }

    /**
     * @return the name of the option in long form
     */
    @Nonnull
    public Optional<String> getLongOption() {
        return this.longOption;
    }

    /**
     * @return the name of the argument for the option
     */
    @Nonnull
    public Optional<String> getArgName() {
        return this.argName;
    }

    /**
     * @return the number of arguments expected in this option
     */
    public int getArguments() {
        return this.arguments;
    }

    /**
     * @return whether this option is required
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * @return whether this option supports an optional argument
     */
    public boolean hasOptionalArg() {
        return this.optionalArg;
    }

    /**
     * @return the completer to use when tab-completing this option
     */
    @Nonnull
    public Optional<Completer> getCompleter() {
        return this.completer;
    }

    /**
     * @return the commons-cli option implementation corresponding to this object
     */
    @Nonnull
    public org.apache.commons.cli.Option asOption() {
        final org.apache.commons.cli.Option option =
                new org.apache.commons.cli.Option(getShortOption(), getDescription());
        if (getLongOption().isPresent()) {
            option.setLongOpt(getLongOption().get());
        }
        if (getArgName().isPresent()) {
            option.setArgName(getArgName().get());
        }
        option.setArgs(getArguments());
        option.setRequired(isRequired());
        option.setOptionalArg(hasOptionalArg());
        return option;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("description", getDescription());
        str.append("shortOption", getShortOption());
        str.append("longOption", getLongOption());
        str.append("argName", getArgName());
        str.append("arguments", getArguments());
        str.append("required", isRequired());
        str.append("optionalArg", hasOptionalArg());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final Option other) {
        if (other == null) {
            return 1;
        }

        final OptionalComparator<String> optionalComparatorString = new OptionalComparator<>();
        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getDescription(), other.getDescription());
        cmp.append(getShortOption(), other.getShortOption());
        cmp.append(getLongOption(), other.getLongOption(), optionalComparatorString);
        cmp.append(getArgName(), other.getArgName(), optionalComparatorString);
        cmp.append(getArguments(), other.getArguments());
        cmp.append(isRequired(), other.isRequired());
        cmp.append(hasOptionalArg(), other.hasOptionalArg());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof Option) && compareTo((Option) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getDescription());
        hash.append(getShortOption());
        hash.append(getLongOption());
        hash.append(getArgName());
        hash.append(getArguments());
        hash.append(isRequired());
        hash.append(hasOptionalArg());
        return hash.toHashCode();
    }
}
