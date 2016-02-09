package microsys.shell.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import microsys.common.config.CommonConfig;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.ConsoleManager;
import microsys.shell.RegistrationManager;
import microsys.shell.model.Option;
import microsys.shell.model.Options;
import microsys.shell.model.ShellEnvironment;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Launch the shell.
 */
public class Runner {
    private CuratorFramework curator;
    private ConsoleManager consoleManager;
    private ShellEnvironment shellEnvironment;

    /**
     * @throws Exception if there is a problem creating the curator framework
     */
    protected Runner(final Config config) throws Exception {
        this.curator = createCurator(config);

        this.shellEnvironment = getShellEnvironment(config, curator);
        this.consoleManager = new ConsoleManager(config, shellEnvironment);
    }

    protected void setConsoleManager(final ConsoleManager consoleManager) {
        this.consoleManager = Objects.requireNonNull(consoleManager);
    }

    protected ConsoleManager getConsoleManager() {
        return this.consoleManager;
    }

    protected void setShellEnvironment(final ShellEnvironment shellEnvironment) {
        this.shellEnvironment = shellEnvironment;
    }

    protected ShellEnvironment getShellEnvironment() {
        return this.shellEnvironment;
    }

    protected void run(final File file) throws Exception {
        getConsoleManager().run(Objects.requireNonNull(file));
    }

    protected void run(final String command) throws Exception {
        getConsoleManager().run(Objects.requireNonNull(command));
    }

    protected void run() throws Exception {
        // Blocks until the shell is finished.
        getConsoleManager().run();
    }

    protected void shutdown() {
        this.shellEnvironment.close();
    }

    protected ShellEnvironment getShellEnvironment(final Config config, final CuratorFramework curator)
            throws Exception {
        final ExecutorService executor =
                Executors.newFixedThreadPool(config.getInt(CommonConfig.EXECUTOR_THREADS.getKey()));
        final DiscoveryManager discoveryManager = new DiscoveryManager(config, curator);
        final RegistrationManager registrationManager = new RegistrationManager();
        final OkHttpClient httpClient = new OkHttpClient.Builder().build();
        final ShellEnvironment shellEnvironment =
                new ShellEnvironment(config, executor, discoveryManager, curator, registrationManager, httpClient);
        registrationManager.loadCommands(shellEnvironment);
        return shellEnvironment;
    }

    protected CuratorFramework createCurator(final Config config) throws Exception {
        final String zookeepers = config.getString(CommonConfig.ZOOKEEPER_HOSTS.getKey());
        final String namespace = config.getString(CommonConfig.SYSTEM_NAME.getKey());
        final CuratorFramework curator =
                CuratorFrameworkFactory.builder().connectString(zookeepers).namespace(namespace)
                        .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        if (!curator.blockUntilConnected(2, TimeUnit.SECONDS)) {
            throw new Exception("Failed to connect to zookeeper");
        }
        return curator;
    }

    protected static void processCommandLine(final Runner runner, final String[] args) throws Exception {
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
    public static void main(final String... args) throws Exception {
        Runner.processCommandLine(new Runner(ConfigFactory.load()), args);
    }
}
