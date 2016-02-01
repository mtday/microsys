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

/**
 * This actor implements the {@code help} command in the shell.
 */
public class HelpCommand extends Command {
    /**
     * @param shellEnvironment the shell command execution environment
     */
    public HelpCommand(final ShellEnvironment shellEnvironment) {
        super(shellEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> getRegistrations() {
        final Optional<String> description = Optional.of("display usage information for available shell commands");
        final CommandPath help = new CommandPath("help");
        return Collections.singletonList(new Registration(help, Optional.empty(), description));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CommandStatus process(final UserCommand userCommand, final PrintWriter writer) {
        final CommandPath path = userCommand.getCommandPath();
        if (path.getSize() > 1 && path.isPrefix(new CommandPath("help"))) {
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

    protected void handleRegistrations(final Set<Registration> registrations, final PrintWriter writer) {
        // Only show options for help with a single command.
        final boolean includeOptions = registrations.size() == 1;

        // Determine the longest command path length to better format the output.
        final OptionalInt longestPath = registrations.stream().mapToInt(r -> r.getPath().toString().length()).max();

        final List<String> output = new LinkedList<>();
        registrations.forEach(r -> output.addAll(getOutput(r, includeOptions, longestPath)));
        output.forEach(writer::println);
    }

    protected List<String> getOutput(
            final Registration registration, final boolean includeOptions, final OptionalInt longestPath) {
        final List<String> output = new LinkedList<>();
        output.add(getDescription(registration, longestPath));
        if (includeOptions) {
            output.addAll(getOptions(registration));
        }
        return output;
    }

    protected String getDescription(final Registration registration, final OptionalInt longestPath) {
        if (registration.getDescription().isPresent()) {
            final String path = StringUtils.rightPad(registration.getPath().toString(), longestPath.getAsInt());
            return String.format("  %s  %s", path, registration.getDescription().get());
        }
        return String.format("  %s", registration.getPath().toString());
    }

    protected List<String> getOptions(final Registration registration) {
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
                if (option.isRequired()) {
                    str.append("  (required)");
                }
                if (option.getDescription() != null) {
                    str.append("  ");
                    str.append(option.getDescription());
                }
                output.add(str.toString());
            }
        }
        return output;
    }
}
