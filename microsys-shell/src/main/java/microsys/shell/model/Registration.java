package microsys.shell.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jline.console.completer.Completer;
import microsys.shell.completer.CompletionTree;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * An immutable representation of a command registration available for use within the shell.
 */
public class Registration implements Comparable<Registration> {
    private final CommandPath path;
    private final Optional<Options> options;
    private final Optional<String> description;

    /**
     * @param path        the fully qualified path to the command
     * @param options     the options available for the command
     * @param description a description of the command defined in this registration
     */
    public Registration(final CommandPath path, final Optional<Options> options, final Optional<String> description) {
        this.path = Objects.requireNonNull(path);
        this.options = Objects.requireNonNull(options);
        this.description = Objects.requireNonNull(description);
    }

    /**
     * @return the fully qualified path to the command
     */
    public CommandPath getPath() {
        return this.path;
    }

    /**
     * @return the options available for the command
     */
    public Optional<Options> getOptions() {
        return this.options;
    }

    /**
     * @return a description of the command defined in this registration
     */
    public Optional<String> getDescription() {
        return this.description;
    }

    /**
     * @return the completion tree representing the possible tab-completions available for this command and its options
     */
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
     * @return a {@link Completer} capable of performing tab-completion for this registration and the command it defines
    public Completer getCompleter() {
        final List<Completer> completers = new ArrayList<>();
        // First add the completers for the command path.
        completers.addAll(getPath().getCompleters());

        // Second, add the completers for the options as an aggregate.
        if (getOptions().isPresent()) {
            completers.add(getOptions().get().getCompleter());
        }

        // Lastly, end the list of completers.
        completers.add(new NullCompleter());

        return new ArgumentCompleter(new CustomArgumentDelimiter(), completers);
    }

    public static class CustomArgumentDelimiter extends ArgumentCompleter.WhitespaceArgumentDelimiter {
        @Override
        public ArgumentCompleter.ArgumentList delimit(CharSequence buffer, int pos) {
            return new CustomArgumentList(super.delimit(buffer, pos));
        }

        public static class CustomArgumentList extends ArgumentCompleter.ArgumentList {
            public CustomArgumentList(final ArgumentCompleter.ArgumentList list) {
                super(
                        list.getArguments(), list.getCursorArgumentIndex(), list.getArgumentPosition(),
                        list.getBufferPosition());
            }

            @Override
            public String getCursorArgument() {
                return StringUtils.trimToEmpty(super.getCursorArgument());
            }
        }
    }
     */

    /**
     * {@inheritDoc}
     */
    @Override
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
    public boolean equals(final Object other) {
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
