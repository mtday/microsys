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

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Launch the shell.
 */
public class Runner {
    private CuratorFramework curator;
    private ShellEnvironment shellEnvironment;
    private ConsoleManager consoleManager;

    /**
     * @throws Exception if there is a problem creating the curator framework
     */
    protected Runner(final Config config) throws Exception {
        this.curator = createCurator(config);
        this.shellEnvironment = getShellEnvironment(config, curator);
        this.consoleManager = new ConsoleManager(config, shellEnvironment.getRegistrationManager());
    }

    protected void setConsoleManager(final ConsoleManager consoleManager) {
        this.consoleManager = Objects.requireNonNull(consoleManager);
    }

    protected void run(final File file) throws Exception {
        // Blocks until the shell is finished.
        this.consoleManager.run(Objects.requireNonNull(file));
    }

    protected void run() throws Exception {
        // Blocks until the shell is finished.
        this.consoleManager.run();
    }

    protected void shutdown() {
        this.curator.close();
    }

    protected ShellEnvironment getShellEnvironment(final Config config, final CuratorFramework curator)
            throws Exception {
        final DiscoveryManager discoveryManager = new DiscoveryManager(config, curator);
        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment =
                new ShellEnvironment(config, discoveryManager, curator, registrationManager);
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
                        false, false);
        final Options options = new Options(fileOption);

        try {
            final CommandLine commandLine = new DefaultParser().parse(options.asOptions(), args);

            if (commandLine.hasOption("f")) {
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
