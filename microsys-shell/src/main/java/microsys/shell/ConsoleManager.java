package microsys.shell;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.UserInterruptException;
import jline.console.history.FileHistory;
import microsys.common.config.CommonConfig;
import microsys.shell.completer.ShellCompleter;
import microsys.shell.model.Command;
import microsys.shell.model.CommandPath;
import microsys.shell.model.CommandStatus;
import microsys.shell.model.Registration;
import microsys.shell.model.ShellEnvironment;
import microsys.shell.model.Token;
import microsys.shell.model.UserCommand;
import microsys.shell.util.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.SortedSet;

/**
 * Responsible for managing the console reader, along with user input and console output.
 */
public class ConsoleManager {
    private final static String PROMPT = "shell> ";

    private final Config config;
    private final ShellEnvironment shellEnvironment;
    private final ConsoleReader consoleReader;
    private final Optional<FileHistory> fileHistory;
    private final Optional<ShellCompleter> completer;

    /**
     * @param config the static system configuration information
     * @param shellEnvironment the shell environment
     * @throws IOException if there is a problem creating the console reader
     */
    public ConsoleManager(final Config config, final ShellEnvironment shellEnvironment) throws IOException {
        this.config = Objects.requireNonNull(config);
        this.shellEnvironment = Objects.requireNonNull(shellEnvironment);

        this.consoleReader = new ConsoleReader();
        this.consoleReader.setHandleUserInterrupt(true);
        this.consoleReader.setPaginationEnabled(true);
        this.consoleReader.setPrompt(PROMPT);

        this.fileHistory = Optional.of(createHistory(this.config));
        this.consoleReader.setHistory(this.fileHistory.get());

        this.completer = Optional.of(new ShellCompleter(shellEnvironment.getRegistrationManager()));
        this.consoleReader.addCompleter(this.completer.get());
    }

    /**
     * @param config the static system configuration information
     * @param shellEnvironment the shell environment
     * @param consoleReader the {@link ConsoleReader} used to retrieve input from the user
     */
    @VisibleForTesting
    public ConsoleManager(
            final Config config, final ShellEnvironment shellEnvironment, final ConsoleReader consoleReader) {
        this.config = Objects.requireNonNull(config);
        this.shellEnvironment = Objects.requireNonNull(shellEnvironment);
        this.consoleReader = Objects.requireNonNull(consoleReader);
        this.fileHistory = Optional.empty();
        this.completer = Optional.empty();
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the shell environment
     */
    protected ShellEnvironment getShellEnvironment() {
        return this.shellEnvironment;
    }

    /**
     * @return the console reader used to manage user input and console output
     */
    protected ConsoleReader getConsoleReader() {
        return this.consoleReader;
    }

    /**
     * @return the file history, possibly empty if history is not being managed
     */
    protected Optional<FileHistory> getHistory() {
        return this.fileHistory;
    }

    protected FileHistory createHistory(final Config config) throws IOException {
        final String userHome = config.getString("user.home");
        final String systemName = config.getString(CommonConfig.SYSTEM_NAME.getKey());
        final String historyFileName = config.getString(CommonConfig.SHELL_HISTORY_FILE.getKey());
        return new FileHistory(new File(String.format("%s/.%s/%s", userHome, systemName, historyFileName)));
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
     * @param command the command specified on the command line to be executed
     * @throws IOException if there is a problem reading from or writing to the console
     */
    public void run(final String command) throws IOException {
        handleInput(Optional.ofNullable(command), true);
        stop();
    }

    /**
     * @param file the input file from which commands should be read and processed
     * @throws IOException if there is a problem reading from or writing to the console
     */
    public void run(final File file) throws IOException {
        if (!Objects.requireNonNull(file).exists()) {
            // No file so no need to do anything.
            stop();
            return;
        }

        try (final Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.name())) {
            CommandStatus commandStatus = CommandStatus.SUCCESS;
            while (commandStatus != CommandStatus.TERMINATE && scanner.hasNextLine()) {
                // Continue accepting input until a TERMINATE is returned or we hit the end of the file.
                commandStatus = handleInput(Optional.ofNullable(scanner.nextLine()), true);
            }
        }

        stop();
    }

    /**
     * @throws IOException if there is a problem reading from or writing to the console
     */
    public void run() throws IOException {
        printStartupOutput();

        CommandStatus commandStatus = CommandStatus.SUCCESS;
        while (commandStatus != CommandStatus.TERMINATE) {
            try {
                // Continue accepting input until a TERMINATE is returned.
                commandStatus = handleInput(Optional.ofNullable(getConsoleReader().readLine()), false);
            } catch (final UserInterruptException ctrlC) {
                if (!StringUtils.isBlank(ctrlC.getPartialLine())) {
                    // The user typed Ctrl-C with a partial command in the line, continue with new input.
                    commandStatus = CommandStatus.SUCCESS;
                } else {
                    // The user typed Ctrl-C with no text in the line, terminate.
                    commandStatus = CommandStatus.TERMINATE;
                }
            }
        }

        stop();
    }

    protected void stop() throws IOException {
        if (getHistory().isPresent()) {
            getHistory().get().flush();
        }
        getConsoleReader().println();
        getConsoleReader().getTerminal().setEchoEnabled(true);
        getConsoleReader().shutdown();
        try {
            TerminalFactory.get().restore();
        } catch (final Exception restoreException) {
            // Ignored, we are stopping any way.
        }
        getShellEnvironment().close();
    }

    protected CommandStatus handleInput(final Optional<String> input, final boolean printCommand) throws IOException {
        if (!input.isPresent()) {
            // User typed Ctrl-D, terminate.
            return CommandStatus.TERMINATE;
        } else {
            final String userInput = StringUtils.trimToEmpty(input.get());

            if (userInput.isEmpty() || userInput.startsWith("#")) {
                return CommandStatus.SUCCESS;
            }

            try {
                if (printCommand) {
                    getConsoleReader().println(String.format("# %s", userInput));
                }
                return handleTokens(Tokenizer.tokenize(userInput));
            } catch (final ParseException badInput) {
                // The error offset is always set in ParseExceptions thrown from Tokenizer.tokenize.
                final int realOffset = badInput.getErrorOffset() + PROMPT.length();
                getConsoleReader().println(StringUtils.leftPad("^", realOffset, "-"));
                getConsoleReader().println(badInput.getMessage());
                getConsoleReader().flush();
                return CommandStatus.FAILED;
            }
        }
    }

    protected CommandStatus handleTokens(final List<Token> tokens) throws IOException {
        final CommandPath commandPath = new CommandPath(tokens);
        final SortedSet<Registration> registrations =
                getShellEnvironment().getRegistrationManager().getRegistrations(commandPath);

        CommandStatus returnStatus = CommandStatus.SUCCESS;
        if (registrations.isEmpty()) {
            getConsoleReader().println("Unrecognized command: " + commandPath);
            getConsoleReader().println("Use 'help' to see all the available commands.");
            getConsoleReader().flush();
        } else if (registrations.size() > 1) {
            // Multiple matching commands. Send to the "help" command to process.
            final Registration help =
                    getShellEnvironment().getRegistrationManager().getRegistrations(new CommandPath("help")).first();
            final List<String> path = new LinkedList<>(commandPath.getPath());
            path.add(0, "help");
            final UserCommand userCommand = new UserCommand(new CommandPath(path), help, tokens);
            returnStatus = getShellEnvironment().getRegistrationManager().getCommand(help).get()
                    .process(userCommand, new PrintWriter(getConsoleReader().getOutput(), true));
        } else {
            // A single matching command, run it.
            final Registration registration = registrations.iterator().next();

            if (!registration.getPath().equals(commandPath)) {
                getConsoleReader().println("Assuming you mean: " + registration.getPath());
            }

            // This command will always exist.
            final Optional<Command> command = getShellEnvironment().getRegistrationManager().getCommand(registration);
            final UserCommand userCommand = new UserCommand(commandPath, registration, tokens);
            try {
                userCommand.validateCommandLine();
                returnStatus =
                        command.get().process(userCommand, new PrintWriter(getConsoleReader().getOutput(), true));
            } catch (final org.apache.commons.cli.ParseException invalidParams) {
                getConsoleReader().println(invalidParams.getMessage());
                returnStatus = CommandStatus.FAILED;
            }

            getConsoleReader().flush();
        }
        return returnStatus;
    }
}
