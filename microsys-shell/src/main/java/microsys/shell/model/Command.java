package microsys.shell.model;

import com.typesafe.config.Config;
import microsys.service.discovery.DiscoveryManager;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public abstract class Command {
    private final Config config;
    private final DiscoveryManager discoveryManager;

    public Command(final Config config, final DiscoveryManager discoveryManager) {
        this.config = Objects.requireNonNull(config);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
    }

    protected Config getConfig() {
        return this.config;
    }

    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    public abstract List<Registration> getRegistrations();

    public abstract boolean process(UserCommand userCommand);
}
