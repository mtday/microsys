package microsys.shell.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.shell.completer.CompletionTree;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable representation of a command registration available for use within the shell.
 */
public class Registration implements Comparable<Registration> {
    @Nonnull
    private final CommandPath path;
    @Nonnull
    private final Optional<Options> options;
    @Nonnull
    private final Optional<String> description;

    /**
     * @param path        the fully qualified path to the command
     * @param options     the options available for the command
     * @param description a description of the command defined in this registration
     */
    public Registration(
            @Nonnull final CommandPath path, @Nonnull final Optional<Options> options,
            @Nonnull final Optional<String> description) {
        this.path = Objects.requireNonNull(path);
        this.options = Objects.requireNonNull(options);
        this.description = Objects.requireNonNull(description);
    }

    /**
     * @return the fully qualified path to the command
     */
    @Nonnull
    public CommandPath getPath() {
        return this.path;
    }

    /**
     * @return the options available for the command
     */
    @Nonnull
    public Optional<Options> getOptions() {
        return this.options;
    }

    /**
     * @return a description of the command defined in this registration
     */
    @Nonnull
    public Optional<String> getDescription() {
        return this.description;
    }

    /**
     * @return the completion tree representing the possible tab-completions available for this command and its options
     */
    @Nonnull
    public CompletionTree getCompletions() {
        final CompletionTree root = new CompletionTree();
        CompletionTree current = root;
        for (final String path : getPath().getPath()) {
            final CompletionTree child = new CompletionTree(path);
            current.add(child);
            current = child;
        }

        if (getOptions().isPresent()) {
            current.add(getOptions().get().getCompletions());
        }

        return root;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("path", getPath());
        str.append("options", getOptions());
        str.append("description", getDescription());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final Registration registration) {
        if (registration == null) {
            return 1;
        }

        return getPath().compareTo(registration.getPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof Registration) && compareTo((Registration) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getPath().hashCode();
    }
}
