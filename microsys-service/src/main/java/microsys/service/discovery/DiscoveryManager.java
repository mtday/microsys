package microsys.service.discovery;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.common.model.ServiceType;
import microsys.service.model.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 */
public class DiscoveryManager {
    private final static Logger LOG = LoggerFactory.getLogger(DiscoveryManager.class);

    private final Config config;
    private final ServiceDiscovery<String> discovery;

    /**
     * @param config the static system configuration information
     * @param curator the {@link CuratorFramework} that is managing zookeeper operations
     */
    public DiscoveryManager(final Config config, final CuratorFramework curator) throws Exception {
        this.config = Objects.requireNonNull(config);

        this.discovery = ServiceDiscoveryBuilder.builder(String.class).client(curator).basePath(getBasePath()).build();
        this.discovery.start();
    }

    protected Config getConfig() {
        return this.config;
    }

    protected ServiceDiscovery<String> getDiscovery() {
        return this.discovery;
    }

    protected String getBasePath() {
        return String.format("/discovery");
    }

    public void register(final Service service) throws Exception {
        final ServiceInstance<String> serviceInstance = service.asServiceInstance();
        try {
            getDiscovery().registerService(serviceInstance);
        } catch (final Exception exception) {
            LOG.error("Failed to register service", exception);
        }
    }

    public SortedSet<Service> getAll() {
        final SortedSet<Service> services = new TreeSet<>();
        Arrays.asList(ServiceType.values()).forEach(t -> services.addAll(getAll(t)));
        return services;
    }

    public SortedSet<Service> getAll(final ServiceType serviceType) {
        final SortedSet<Service> services = new TreeSet<>();
        try {
            final List<ServiceInstance<String>> list =
                    new ArrayList<>(getDiscovery().queryForInstances(Objects.requireNonNull(serviceType).name()));
            list.stream().map(Service::new).forEach(services::add);
        } catch (final Exception exception) {
            LOG.error("Failed to query for services", exception);
        }
        return services;
    }

    public Optional<Service> getRandom(final ServiceType serviceType) {
        try {
            final List<ServiceInstance<String>> services =
                    new ArrayList<>(getDiscovery().queryForInstances(Objects.requireNonNull(serviceType).name()));
            if (!services.isEmpty()) {
                // Return a random service instance from the list.
                return Optional.of(new Service(services.get(new Random().nextInt(services.size()))));
            }
        } catch (final Exception exception) {
            LOG.error("Failed to query for services", exception);
        }
        return Optional.empty();
    }
}
