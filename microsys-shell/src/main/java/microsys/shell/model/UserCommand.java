package microsys.shell.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import microsys.common.model.Model;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The immutable parsed user command to be executed.
 */
public class UserCommand implements Model, Comparable<UserCommand> {
    private final CommandPath commandPath;
    private final Registration registration;
    private final TokenizedUserInput userInput;

    /**
     * @param commandPath the {@link CommandPath} representing the user-specified command
     * @param registration the {@link Registration} associated with the command being invoked
     * @param userInput the {@link TokenizedUserInput} entered in the shell to be executed
     */
    public UserCommand(
            final CommandPath commandPath, final Registration registration, final TokenizedUserInput userInput) {
        this.commandPath = commandPath;
        this.registration = registration;
        this.userInput = userInput;
    }

    public UserCommand(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("commandPath"), "Command path is required");
        Preconditions.checkArgument(json.get("commandPath").isJsonObject(), "Command path must be an object");
        Preconditions.checkArgument(json.has("registration"), "Registration is required");
        Preconditions.checkArgument(json.get("registration").isJsonObject(), "Registration must be an object");
        Preconditions.checkArgument(json.has("userInput"), "User input is required");
        Preconditions.checkArgument(json.get("userInput").isJsonObject(), "User input must be an object");

        this.commandPath = new CommandPath(json.getAsJsonObject("commandPath"));
        this.registration = new Registration(json.getAsJsonObject("registration"));
        this.userInput = new TokenizedUserInput(json.getAsJsonObject("userInput"));
    }

    /**
     * @return the {@link CommandPath} representing the user-specified command
     */
    public CommandPath getCommandPath() {
        return this.commandPath;
    }

    /**
     * @return the {@link Registration} associated with the command being invoked
     */
    public Registration getRegistration() {
        return this.registration;
    }

    /**
     * @return the {@link UserInput} entered in the shell to be executed
     */
    public TokenizedUserInput getUserInput() {
        return this.userInput;
    }

    /**
     * @return the parsed {@link CommandLine} parameters for this command based on the user input and registration
     * options
     */
    public Optional<CommandLine> getCommandLine() {
        try {
            return parseCommandLine(getRegistration(), getUserInput());
        } catch (final ParseException parseException) {
            // This exception is suppressed. The validateCommandLine method is used to determine whether the command
            // line is valid or not.
            return Optional.empty();
        }
    }

    /**
     * @throws ParseException if the command line parameters are invalid for some reason
     */
    public void validateCommandLine() throws ParseException {
        parseCommandLine(getRegistration(), getUserInput());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.add("commandPath", getCommandPath().toJson());
        json.add("registration", getRegistration().toJson());
        json.add("userInput", getUserInput().toJson());
        return json;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("commandPath", getCommandPath());
        str.append("registration", getRegistration());
        str.append("userInput", getUserInput());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final UserCommand other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getCommandPath(), other.getCommandPath());
        cmp.append(getRegistration(), other.getRegistration());
        cmp.append(getUserInput(), other.getUserInput());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof UserCommand) && compareTo((UserCommand) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getRegistration().hashCode();
    }

    private static Optional<CommandLine> parseCommandLine(
            final Registration registration, final TokenizedUserInput userInput) throws ParseException {
        if (registration.getOptions().isPresent()) {
            final List<String> tokens = userInput.getTokens();
            final String[] array = tokens.toArray(new String[tokens.size()]);
            return Optional.of(new DefaultParser().parse(registration.getOptions().get().asOptions(), array));
        } else {
            return Optional.empty();
        }
    }
}
