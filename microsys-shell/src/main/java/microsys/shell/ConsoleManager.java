package microsys.shell;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import jline.console.ConsoleReader;
import microsys.common.config.CommonConfig;
import microsys.shell.model.TokenizedUserInput;
import microsys.shell.model.UserInput;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.Objects;
import java.util.Optional;

/**
 * Responsible for managing the console reader, along with user input and console output.
 */
public class ConsoleManager {
    private final Config config;
    private final ConsoleReader consoleReader;

    /**
     * @param config the static system configuration information
     * @throws IOException if there is a problem creating the console reader
     */
    public ConsoleManager(final Config config) throws IOException {
        this.config = Objects.requireNonNull(config);
        this.consoleReader = new ConsoleReader();
        this.consoleReader.setHandleUserInterrupt(true);
        this.consoleReader.setPaginationEnabled(true);
        this.consoleReader.setPrompt("shell> ");
    }

    /**
     * @param config the static system configuration information
     * @param consoleReader the {@link ConsoleReader} used to retrieve input from the user
     */
    @VisibleForTesting
    protected ConsoleManager(final Config config, final ConsoleReader consoleReader) {
        this.config = Objects.requireNonNull(config);
        this.consoleReader = Objects.requireNonNull(consoleReader);
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
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
    protected void run() throws IOException {
        printStartupOutput();

        boolean done = false;
        while (!done) {
            final Optional<String> input = Optional.ofNullable(getConsoleReader().readLine());
            if (!input.isPresent()) {
                // User typed Ctrl-D
                done = true;
            } else {
                final UserInput userInput = new UserInput(input.get());

                if (!userInput.isComment() && !userInput.isEmpty()) {
                    handleInput(userInput);

                }
            }
        }

        getConsoleReader().println();
        getConsoleReader().getTerminal().setEchoEnabled(true);
        getConsoleReader().shutdown();
    }

    protected void handleInput(final UserInput userInput) throws IOException {
        try {
            final TokenizedUserInput tokenized = new TokenizedUserInput(userInput);

        } catch (final ParseException badInput) {
            if (badInput.getErrorOffset() > 0) {
                getConsoleReader().println(StringUtils.leftPad("^", badInput.getErrorOffset(), "-"));
            }
            getConsoleReader().println(badInput.getMessage());
            getConsoleReader().flush();
        }
    }

    protected void handleUnrecognizedCommand(final TokenizedUserInput tokenized) throws IOException {
        getConsoleReader().println("The specified command was not recognized: TODO");
        getConsoleReader().println("Use 'help' to see all the available commands.");
        getConsoleReader().flush();
    }
}
