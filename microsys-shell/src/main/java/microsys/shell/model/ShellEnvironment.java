package microsys.shell.model;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;

import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;

import java.util.Objects;

/**
 * Provides shell environmental configuration and utilities for use within commands.
 */
public class ShellEnvironment {
    private final Config config;
    private final DiscoveryManager discoveryManager;
    private final CuratorFramework curatorFramework;
    private final RegistrationManager registrationManager;

    /**
     * @param config the static system configuration information
     * @param discoveryManager the service discovery manager used to find and manage available micro services
     * @param curatorFramework the curator framework used to manage interactions with zookeeper
     * @param registrationManager the manager used to track the available command registrations
     */
    public ShellEnvironment(
            final Config config, final DiscoveryManager discoveryManager, final CuratorFramework curatorFramework,
            final RegistrationManager registrationManager) {
        this.config = Objects.requireNonNull(config);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.curatorFramework = Objects.requireNonNull(curatorFramework);
        this.registrationManager = Objects.requireNonNull(registrationManager);
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the service discovery manager used to find and manage available micro services
     */
    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    /**
     * @return the curator framework used to manage interactions with zookeeper
     */
    public CuratorFramework getCuratorFramework() {
        return this.curatorFramework;
    }

    /**
     * @return the manager used to track the available command registrations
     */
    public RegistrationManager getRegistrationManager() {
        return this.registrationManager;
    }
}
