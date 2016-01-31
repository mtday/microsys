package microsys.shell.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.util.CollectionComparator;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An immutable class used to manage the options available to a command.
 */
public class Options implements Comparable<Options> {
    private final SortedSet<Option> options = new TreeSet<>();

    /**
     * @param options the individual option objects supported for the command
     */
    public Options(final Collection<Option> options) {
        this.options.addAll(Objects.requireNonNull(options));
    }

    /**
     * @param options the individual option objects supported for the command
     */
    public Options(final Option... options) {
        this(Arrays.asList(Objects.requireNonNull(options)));
    }

    /**
     * @return an unmodifiable sorted set of the options supported for the command
     */
    public SortedSet<Option> getOptions() {
        return Collections.unmodifiableSortedSet(this.options);
    }

    /**
     * @return the commons-cli options implementation corresponding to this object
     */
    public org.apache.commons.cli.Options asOptions() {
        final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        getOptions().stream().map(Option::asOption).forEach(options::addOption);
        return options;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("options", getOptions());
        return str.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final Options other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getOptions(), other.getOptions(), new CollectionComparator<>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof Options) && compareTo((Options) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getOptions());
        return hash.toHashCode();
    }
}
