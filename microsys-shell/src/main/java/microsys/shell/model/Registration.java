package microsys.shell.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.annotation.Nullable;
import java.util.Optional;

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
    public Registration(final CommandPath path, final Optional<Options> options,
                         final Optional<String> description) {
        this.path = path;
        this.options = options;
        this.description = description;
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
