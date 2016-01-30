package microsys.shell.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import microsys.common.model.Model;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * An immutable representation of unprocessed user input received from the shell interface.
 */
public class UserInput implements Model, Comparable<UserInput> {
    private final String input;

    /**
     * @param input the unprocessed user-provided input from the shell interface
     */
    public UserInput(final String input) {
        this.input = StringUtils.trimToEmpty(Objects.requireNonNull(input));
    }

    /**
     * @param json the json object from which this user input will be created
     */
    public UserInput(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("input"), "Input is required");
        Preconditions.checkArgument(json.get("input").isJsonPrimitive(), "Input must be a primitive");

        this.input = StringUtils.trimToEmpty(json.getAsJsonPrimitive("input").getAsString());
    }


    /**
     * @return the unprocessed user-provided input from the shell interface
     */
    public String getInput() {
        return this.input;
    }

    /**
     * @return whether the user input is empty
     */
    public boolean isEmpty() {
        return getInput().isEmpty();
    }

    /**
     * @return whether the user input is a comment
     */
    public boolean isComment() {
        return getInput().startsWith("#");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("input", getInput());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getInput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final UserInput other) {
        if (other == null) {
            return 1;
        }

        return getInput().compareTo(other.getInput());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof UserInput) && compareTo((UserInput) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getInput().hashCode();
    }
}
