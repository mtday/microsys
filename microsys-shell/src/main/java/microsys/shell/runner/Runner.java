package microsys.shell.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import microsys.common.config.CommonConfig;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.ConsoleManager;
import microsys.shell.RegistrationManager;
import microsys.shell.model.ShellEnvironment;

import java.util.concurrent.TimeUnit;

/**
 * Launch the shell.
 */
public class Runner {
    /**
     * @throws Exception if there is a problem creating the curator framework
     */
    protected Runner() throws Exception {
        final Config config = ConfigFactory.load();
        final CuratorFramework curator = createCurator(config);
        final DiscoveryManager discoveryManager = new DiscoveryManager(config, curator);
        final RegistrationManager registrationManager = new RegistrationManager();
        final ShellEnvironment shellEnvironment =
                new ShellEnvironment(config, discoveryManager, curator, registrationManager);
        registrationManager.loadCommands(shellEnvironment);

        final ConsoleManager consoleManager = new ConsoleManager(config, registrationManager);

        // Blocks until the shell is finished.
        consoleManager.run();

        curator.close();
    }

    protected CuratorFramework createCurator(final Config config) throws Exception {
        final String zookeepers = config.getString(CommonConfig.ZOOKEEPER_HOSTS.getKey());
        final String namespace = config.getString(CommonConfig.SYSTEM_NAME.getKey());
        final CuratorFramework curator =
                CuratorFrameworkFactory.builder().connectString(zookeepers).namespace(namespace)
                        .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        if (!curator.blockUntilConnected(5, TimeUnit.SECONDS)) {
            throw new Exception("Failed to connect to zookeeper");
        }
        return curator;
    }

    /**
     * @param args the command-line arguments
     * @throws Exception if there is a problem creating the curator framework
     */
    public static void main(final String... args) throws Exception {
        new Runner();
    }
}
