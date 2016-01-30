package microsys.shell.model;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;

import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;

import java.util.Objects;

/**
 *
 */
public class ShellEnvironment {
    private final Config config;
    private final DiscoveryManager discoveryManager;
    private final CuratorFramework curatorFramework;
    private final RegistrationManager registrationManager;

    public ShellEnvironment(
            final Config config, final DiscoveryManager discoveryManager, final CuratorFramework curatorFramework,
            final RegistrationManager registrationManager) {
        this.config = Objects.requireNonNull(config);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.curatorFramework = Objects.requireNonNull(curatorFramework);
        this.registrationManager = Objects.requireNonNull(registrationManager);
    }

    protected Config getConfig() {
        return this.config;
    }

    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    public CuratorFramework getCuratorFramework() {
        return this.curatorFramework;
    }

    public RegistrationManager getRegistrationManager() {
        return this.registrationManager;
    }
}
