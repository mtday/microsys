package microsys.shell.model;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.util.CollectionComparator;
import microsys.shell.completer.CompletionTree;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class used to manage the options available to a command.
 */
public class Options implements Comparable<Options> {
    @Nonnull
    private final SortedSet<Option> options = new TreeSet<>();

    /**
     * @param options the individual option objects supported for the command
     */
    public Options(@Nonnull final Collection<Option> options) {
        this.options.addAll(Objects.requireNonNull(options));
    }

    /**
     * @param options the individual option objects supported for the command
     */
    public Options(@Nonnull final Option... options) {
        this(Arrays.asList(Objects.requireNonNull(options)));
    }

    /**
     * @return an unmodifiable sorted set of the options supported for the command
     */
    @Nonnull
    public SortedSet<Option> getOptions() {
        return Collections.unmodifiableSortedSet(this.options);
    }

    /**
     * @return the commons-cli options implementation corresponding to this object
     */
    @Nonnull
    public org.apache.commons.cli.Options asOptions() {
        final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        getOptions().stream().map(Option::asOption).forEach(options::addOption);
        return options;
    }

    /**
     * @return the possible completion trees available for tab-completing this collection of options
     */
    @Nonnull
    public List<CompletionTree> getCompletions() {
        final List<CompletionTree> list = new LinkedList<>();
        getOptions().forEach(option -> {
            if (option.getCompleter().isPresent()) {
                final CompletionTree comp = new CompletionTree(option.getCompleter().get());

                final CompletionTree shortOpt = new CompletionTree("-" + option.getShortOption());
                shortOpt.add(comp);
                list.add(shortOpt);

                if (option.getLongOption().isPresent()) {
                    final CompletionTree longOpt = new CompletionTree("--" + option.getLongOption().get());
                    longOpt.add(comp);
                    list.add(longOpt);
                }
            } else {
                list.add(new CompletionTree("-" + option.getShortOption()));
                if (option.getLongOption().isPresent()) {
                    list.add(new CompletionTree("--" + option.getLongOption().get()));
                }
            }
        });
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
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
    public boolean equals(@CheckForNull final Object other) {
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
