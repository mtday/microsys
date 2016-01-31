package microsys.shell;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import jline.console.ConsoleReader;
import microsys.common.config.CommonConfig;
import microsys.shell.model.*;
import microsys.shell.util.Tokenizer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;

/**
 * Responsible for managing the console reader, along with user input and console output.
 */
public class ConsoleManager {
    private final static String PROMPT = "shell> ";

    private final Config config;
    private final RegistrationManager registrationManager;
    private final ConsoleReader consoleReader;

    /**
     * @param config              the static system configuration information
     * @param registrationManager the shell command registration manager
     * @throws IOException if there is a problem creating the console reader
     */
    public ConsoleManager(final Config config, final RegistrationManager registrationManager) throws IOException {
        this.config = Objects.requireNonNull(config);
        this.registrationManager = Objects.requireNonNull(registrationManager);

        this.consoleReader = new ConsoleReader();
        this.consoleReader.setHandleUserInterrupt(true);
        this.consoleReader.setPaginationEnabled(true);
        this.consoleReader.setPrompt(PROMPT);
    }

    /**
     * @param config              the static system configuration information
     * @param registrationManager the shell command registration manager
     * @param consoleReader       the {@link ConsoleReader} used to retrieve input from the user
     */
    @VisibleForTesting
    protected ConsoleManager(
            final Config config, final RegistrationManager registrationManager, final ConsoleReader consoleReader) {
        this.config = Objects.requireNonNull(config);
        this.registrationManager = Objects.requireNonNull(registrationManager);
        this.consoleReader = Objects.requireNonNull(consoleReader);
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the shell command registration manager
     */
    protected RegistrationManager getRegistrationManager() {
        return this.registrationManager;
    }

    /**
     * @return the console reader used to manage user input and console output
     */
    protected ConsoleReader getConsoleReader() {
        return this.consoleReader;
    }

    protected void printStartupOutput() throws IOException {
        final String system = getConfig().getString(CommonConfig.SYSTEM_NAME.getKey());
        final String version = getConfig().getString(CommonConfig.SYSTEM_VERSION.getKey());

        getConsoleReader().println();
        getConsoleReader().println(String.format("%s %s", system, version));
        getConsoleReader().println();
        getConsoleReader().println("Type 'help' to list the available commands");
    }

    /**
     * @throws IOException if there is a problem reading from or writing to the console
     */
    public void run() throws IOException {
        printStartupOutput();

        CommandStatus commandStatus = CommandStatus.SUCCESS;
        while (commandStatus != CommandStatus.TERMINATE) {
            // Continue accepting input until a TERMINATE is returned.
            commandStatus = handleInput(Optional.ofNullable(getConsoleReader().readLine()));
        }

        getConsoleReader().println();
        getConsoleReader().getTerminal().setEchoEnabled(true);
        getConsoleReader().shutdown();
    }

    protected CommandStatus handleInput(final Optional<String> input) throws IOException {
        if (!input.isPresent()) {
            // User typed Ctrl-D
            getConsoleReader().println("Received Ctrl-D");
            return CommandStatus.TERMINATE;
        } else {
            final String userInput = StringUtils.trimToEmpty(input.get());

            if (userInput.isEmpty() || userInput.startsWith("#")) {
                return CommandStatus.SUCCESS;
            }

            try {
                return handleTokens(Tokenizer.tokenize(userInput));
            } catch (final ParseException badInput) {
                if (badInput.getErrorOffset() > 0) {
                    final int realOffset = badInput.getErrorOffset() + PROMPT.length();
                    getConsoleReader().println(StringUtils.leftPad("^", realOffset, "-"));
                }
                getConsoleReader().println(badInput.getMessage());
                getConsoleReader().flush();
                return CommandStatus.FAILED;
            }
        }
    }

    protected CommandStatus handleTokens(final List<String> tokens) throws IOException {
        final CommandPath commandPath = new CommandPath(tokens);
        final SortedSet<Registration> registrations = getRegistrationManager().getRegistrations(commandPath);

        if (registrations.isEmpty()) {
            getConsoleReader().println("Unrecognized command: " + commandPath);
            getConsoleReader().println("Use 'help' to see all the available commands.");
            getConsoleReader().flush();
        } else if (registrations.size() > 1) {
            // Multiple matching commands. Stick "help" at the front and send back through.
            return handleInput(Optional.of("help " + commandPath));
        } else {
            // A single matching command, run it.
            final Registration registration = registrations.iterator().next();

            if (!registration.getPath().equals(commandPath)) {
                getConsoleReader().println("Assuming you mean: " + registration.getPath());
            }

            final Optional<Command> command = getRegistrationManager().getCommand(registration);
            if (command.isPresent()) {
                final UserCommand userCommand = new UserCommand(commandPath, registration, tokens);
                try {
                    userCommand.validateCommandLine();
                    return command.get().process(userCommand, new PrintWriter(getConsoleReader().getOutput(), true));
                } catch (final org.apache.commons.cli.ParseException invalidParams) {
                    getConsoleReader().println(invalidParams.getMessage());
                    return CommandStatus.FAILED;
                }
            }

            getConsoleReader().flush();
        }
        return CommandStatus.SUCCESS;
    }
}
