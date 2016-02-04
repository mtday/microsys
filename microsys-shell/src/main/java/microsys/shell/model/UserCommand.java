package microsys.shell.model;

import com.google.common.base.Preconditions;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.util.CollectionComparator;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * The immutable parsed user command to be executed.
 */
public class UserCommand implements Comparable<UserCommand> {
    private final CommandPath commandPath;
    private final Registration registration;
    private final List<String> userInput;

    /**
     * @param commandPath the {@link CommandPath} representing the user-specified command
     * @param registration the {@link Registration} associated with the command being invoked
     * @param userInput the list of tokens comprising the user input to be executed
     */
    public UserCommand(
            final CommandPath commandPath, final Registration registration, final List<?> userInput) {
        this.commandPath = Objects.requireNonNull(commandPath);
        this.registration = Objects.requireNonNull(registration);
        this.userInput = Objects.requireNonNull(userInput).stream().map(String::valueOf).collect(Collectors.toList());
        Preconditions.checkArgument(!userInput.isEmpty(), "User input must be provided");
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
     * @return the tokenized user input entered in the shell to be executed
     */
    public List<String> getUserInput() {
        return Collections.unmodifiableList(this.userInput);
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
    public int compareTo(@Nullable final UserCommand other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getCommandPath(), other.getCommandPath());
        cmp.append(getRegistration(), other.getRegistration());
        cmp.append(getUserInput(), other.getUserInput(), new CollectionComparator<String>());
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
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getCommandPath());
        hash.append(getRegistration());
        hash.append(getUserInput());
        return hash.toHashCode();
    }

    private Optional<CommandLine> parseCommandLine(
            final Registration registration, final List<String> userInput) throws ParseException {
        if (registration.getOptions().isPresent()) {
            final String[] array = userInput.toArray(new String[userInput.size()]);
            return Optional.of(new DefaultParser().parse(registration.getOptions().get().asOptions(), array));
        } else {
            return Optional.empty();
        }
    }
}
