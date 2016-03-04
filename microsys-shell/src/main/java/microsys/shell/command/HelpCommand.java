package microsys.shell.command;

import org.apache.commons.lang3.StringUtils;

import microsys.shell.model.Command;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Option;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.UserCommand;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * This actor implements the {@code help} command in the shell.
 */
public class HelpCommand extends Command {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public HelpCommand(@Nonnull final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<Registration> getRegistrations() {
        final Optional<String> description = Optional.of("display usage information for available shell commands");
        final CommandPath help = new CommandPath("help");
        return Collections.singletonList(new Registration(help, Optional.empty(), description));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CommandStatus process(@Nonnull final UserCommand userCommand, @Nonnull final PrintWriter writer) {
        final CommandPath path = userCommand.getCommandPath();
        if (path.getSize() > 1) {
            // Strip off the "help" at the front and lookup the registrations for which help should be retrieved.
            final CommandPath childPath = path.getChild().get();
            writer.println("Showing help for commands that begin with: " + childPath);
            handleRegistrations(getShellEnvironment().getRegistrationManager().getRegistrations(childPath), writer);
        } else {
            // Request all of the available registrations.
            handleRegistrations(getShellEnvironment().getRegistrationManager().getRegistrations(), writer);
        }
        return CommandStatus.SUCCESS;
    }

    protected void handleRegistrations(
            @Nonnull final Set<Registration> registrations, @Nonnull final PrintWriter writer) {
        // Only show options for help with a single command.
        final boolean includeOptions = registrations.size() == 1;

        // Determine the longest command path length to better format the output.
        final OptionalInt longestPath = registrations.stream().mapToInt(r -> r.getPath().toString().length()).max();

        final List<String> output = new LinkedList<>();
        registrations.forEach(r -> output.addAll(getOutput(r, includeOptions, longestPath)));
        output.forEach(writer::println);
    }

    @Nonnull
    protected List<String> getOutput(
            @Nonnull final Registration registration, final boolean includeOptions,
            @Nonnull final OptionalInt longestPath) {
        final List<String> output = new LinkedList<>();
        output.add(getDescription(registration, longestPath));
        if (includeOptions) {
            output.addAll(getOptions(registration));
        }
        return output;
    }

    @Nonnull
    protected String getDescription(@Nonnull final Registration registration, @Nonnull final OptionalInt longestPath) {
        if (registration.getDescription().isPresent()) {
            final String path = StringUtils.rightPad(registration.getPath().toString(), longestPath.getAsInt());
            return String.format("  %s  %s", path, registration.getDescription().get());
        }
        return String.format("  %s", registration.getPath().toString());
    }

    @Nonnull
    protected List<String> getOptions(@Nonnull final Registration registration) {
        final List<String> output = new LinkedList<>();
        if (registration.getOptions().isPresent()) {
            for (final Option option : registration.getOptions().get().getOptions()) {
                final StringBuilder str = new StringBuilder("    ");
                str.append("-");
                str.append(option.getShortOption());
                if (option.getLongOption().isPresent()) {
                    str.append("  --");
                    str.append(option.getLongOption().get());
                }

                final String argName = option.getArgName().isPresent() ? option.getArgName().get() : "arg";
                if (option.hasOptionalArg()) {
                    str.append(" [");
                    str.append(argName);
                    str.append("]");
                } else if (option.getArguments() == 1) {
                    str.append(" <");
                    str.append(argName);
                    str.append(">");
                } else if (option.getArguments() > 1) {
                    str.append(" <");
                    str.append(option.getArguments());
                    str.append(" arguments>");
                }
                if (option.isRequired()) {
                    str.append("  (required)");
                }
                str.append("  ");
                str.append(option.getDescription());
                output.add(str.toString());
            }
        }
        return output;
    }
}
