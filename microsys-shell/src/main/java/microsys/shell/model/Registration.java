package microsys.shell.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import microsys.common.model.Model;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable representation of a command registration available for use within the shell.
 */
public class Registration implements Model, Comparable<Registration> {
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

    public Registration(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("path"), "Path is required");
        Preconditions.checkArgument(json.get("path").isJsonObject(), "Path must be an object");

        this.path = new CommandPath(json.getAsJsonObject("path"));

        if (json.has("description") && json.get("description").isJsonPrimitive()) {
            this.description = Optional.of(json.getAsJsonPrimitive("description").getAsString());
        } else {
            this.description = Optional.empty();
        }

        if (json.has("options") && json.get("options").isJsonObject()) {
            this.options = Optional.of(new Options(json.getAsJsonObject("options")));
        } else {
            this.options = Optional.empty();
        }
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
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.add("path", getPath().toJson());
        if (getOptions().isPresent()) {
            json.add("options", getOptions().get().toJson());
        }
        if (getDescription().isPresent()) {
            json.addProperty("description", getDescription().get());
        }
        return json;
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
    public int compareTo(final Registration registration) {
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
