package microsys.shell.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import microsys.shell.ConsoleManager;
import microsys.shell.model.Option;
import microsys.shell.model.Options;
import microsys.shell.model.ShellEnvironment;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Launch the shell.
 */
public class Runner {
    @Nonnull
    private ShellEnvironment shellEnvironment;
    @Nonnull
    private ConsoleManager consoleManager;

    /**
     * @throws Exception if there is a problem running the shell
     */
    protected Runner(@Nonnull final Config config) throws Exception {
        this.shellEnvironment = new ShellEnvironment(Objects.requireNonNull(config));
        this.consoleManager = new ConsoleManager(config, this.shellEnvironment);
    }

    protected void setConsoleManager(@Nonnull final ConsoleManager consoleManager) {
        this.consoleManager = Objects.requireNonNull(consoleManager);
    }

    @Nonnull
    protected ConsoleManager getConsoleManager() {
        return this.consoleManager;
    }

    protected void run(@Nonnull final File file) throws IOException {
        getConsoleManager().run(Objects.requireNonNull(file));
    }

    protected void run(@Nonnull final String command) throws IOException {
        getConsoleManager().run(Objects.requireNonNull(command));
    }

    protected void run() throws IOException {
        // Blocks until the shell is finished.
        getConsoleManager().run();
    }

    protected void shutdown() {
        this.shellEnvironment.close();
    }

    protected static void processCommandLine(@Nonnull final Runner runner, @Nonnull final String[] args)
            throws IOException {
        final Option fileOption =
                new Option("run shell commands provided by a file", "f", Optional.of("file"), Optional.of("file"), 1,
                        false, false, Optional.empty());
        final Option commandOption =
                new Option("run the specified shell command", "c", Optional.of("command"), Optional.of("command"), 1,
                        false, false, Optional.empty());
        final Options options = new Options(fileOption, commandOption);

        try {
            final CommandLine commandLine = new DefaultParser().parse(options.asOptions(), args);

            if (commandLine.hasOption("c")) {
                runner.run(commandLine.getOptionValue("c"));
            } else if (commandLine.hasOption("f")) {
                final File file = new File(commandLine.getOptionValue("f"));
                if (file.exists()) {
                    // Run the contents of the file.
                    runner.run(file);
                } else {
                    System.err.println("The specified input file does not exist: " + file.getAbsolutePath());
                }
            } else {
                // Run in interactive mode.
                runner.run();
            }
        } catch (final ParseException invalidParameters) {
            System.err.println(invalidParameters.getMessage());
        }
        runner.shutdown();
    }

    /**
     * @param args the command-line arguments
     * @throws Exception if there is a problem running the shell
     */
    public static void main(@Nonnull final String... args) throws Exception {
        final Config config = ConfigFactory.load().withFallback(ConfigFactory.systemProperties())
                .withFallback(ConfigFactory.systemEnvironment());
        Runner.processCommandLine(new Runner(config), args);
    }
}
