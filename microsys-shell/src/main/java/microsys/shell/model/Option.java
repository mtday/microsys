package microsys.shell.model;

import microsys.common.util.OptionalComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * An immutable class representing a possible option available to a command.
 */
public class Option implements Comparable<Option> {
    private final String description;
    private final String shortOption;
    private final Optional<String> longOption;
    private final Optional<String> argName;
    private final int arguments;
    private final boolean required;
    private final boolean optionalArg;

    /**
     * @param description the description of the option
     * @param shortOption the name of the option in short form
     * @param longOption  the name of the option in long form, possibly empty
     * @param argName     the name of the argument for the option
     * @param arguments   the number of arguments expected with this option
     * @param required    whether this option is required
     * @param optionalArg whether this option supports an optional argument
     */
    public Option(
            final String description, final String shortOption, final Optional<String> longOption,
            final Optional<String> argName, final int arguments, final boolean required, final boolean optionalArg) {
        this.description = description;
        this.shortOption = shortOption;
        this.longOption = longOption;
        this.argName = argName;
        this.arguments = arguments;
        this.required = required;
        this.optionalArg = optionalArg;
    }

    /**
     * @return the description of the option
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the name of the option in short form
     */
    public String getShortOption() {
        return this.shortOption;
    }

    /**
     * @return the name of the option in long form
     */
    public Optional<String> getLongOption() {
        return this.longOption;
    }

    /**
     * @return the name of the argument for the option
     */
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
     * @return the commons-cli option implementation corresponding to this object
     */
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
    public boolean equals(final Object other) {
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