package microsys.shell.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.curator.framework.CuratorFramework;

import microsys.common.config.ConfigKeys;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.curator.CuratorCreator;
import microsys.curator.CuratorException;
import microsys.discovery.DiscoveryException;
import microsys.discovery.DiscoveryManager;
import microsys.discovery.impl.CuratorDiscoveryManager;
import microsys.shell.ConsoleManager;
import microsys.shell.RegistrationManager;
import microsys.shell.model.Option;
import microsys.shell.model.Options;
import microsys.shell.model.ShellEnvironment;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

/**
 * Launch the shell.
 */
public class Runner {
    @Nonnull
    private ConsoleManager consoleManager;
    @Nonnull
    private ShellEnvironment shellEnvironment;

    /**
     * @throws Exception if there is a problem running the shell
     */
    protected Runner(@Nonnull final Config config) throws Exception {
        this.shellEnvironment = getShellEnvironment(config);
        this.consoleManager = new ConsoleManager(config, shellEnvironment);
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

    @Nonnull
    protected OkHttpClient getHttpClient(@Nonnull final CryptoFactory cryptoFactory) throws EncryptionException {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(cryptoFactory.getSSLContext().getSocketFactory());
        return builder.build();
    }

    @Nonnull
    protected ShellEnvironment getShellEnvironment(@Nonnull final Config config)
            throws DiscoveryException, CuratorException, EncryptionException {
        Objects.requireNonNull(config);
        final ExecutorService executor =
                Executors.newFixedThreadPool(config.getInt(ConfigKeys.EXECUTOR_THREADS.getKey()));
        final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);
        final CuratorFramework curator = CuratorCreator.create(config, cryptoFactory);
        final DiscoveryManager discoveryManager = new CuratorDiscoveryManager(config, curator);
        final RegistrationManager registrationManager = new RegistrationManager();
        final OkHttpClient httpClient = getHttpClient(cryptoFactory);
        final ShellEnvironment shellEnvironment =
                new ShellEnvironment(config, executor, discoveryManager, curator, registrationManager, httpClient,
                        cryptoFactory);
        registrationManager.loadCommands(shellEnvironment);
        return shellEnvironment;
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
