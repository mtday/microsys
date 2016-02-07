package microsys.service.discovery;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;

import microsys.common.model.ServiceType;
import microsys.service.model.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * The class is used to manage the system service discovery activity and is responsible for registering and
 * unregistering services, and retrieving available services.
 */
public class DiscoveryManager {
    private final Config config;
    private final ServiceDiscovery<String> discovery;

    private boolean isClosed = false;

    /**
     * @param config the static system configuration information
     * @param curator the {@link CuratorFramework} that is managing zookeeper operations
     * @throws Exception if there is a problem starting the service discovery service
     */
    public DiscoveryManager(final Config config, final CuratorFramework curator) throws Exception {
        this.config = Objects.requireNonNull(config);
        this.discovery = ServiceDiscoveryBuilder.builder(String.class).client(curator).basePath("/discovery").build();
        this.discovery.start();
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the internal curator {@link ServiceDiscovery} object that manages the interaction with zookeeper
     */
    protected ServiceDiscovery<String> getDiscovery() {
        return this.discovery;
    }

    /**
     * @return whether this manager has been closed or not
     */
    public boolean isClosed() {
        return this.isClosed;
    }

    /**
     * Shutdown the resources associated with this service discovery manager.
     */
    public void close() {
        try {
            this.isClosed = true;
            getDiscovery().close();
        } catch (final IOException ignored) {
            // Ignored.
        }
    }

    /**
     * @param service the {@link Service} to register
     * @throws Exception if there is a problem registering the service
     */
    public void register(final Service service) throws Exception {
        final ServiceInstance<String> serviceInstance = Objects.requireNonNull(service).asServiceInstance();
        if (!isClosed()) {
            getDiscovery().registerService(serviceInstance);
        }
    }

    /**
     * @param service the {@link Service} to unregister
     * @throws Exception if there is a problem unregistering the service
     */
    public void unregister(final Service service) throws Exception {
        final ServiceInstance<String> serviceInstance = service.asServiceInstance();
        if (!isClosed()) {
            getDiscovery().unregisterService(serviceInstance);
        }
    }

    /**
     * @return all of the available {@link Service} objects that have registered with service discovery
     */
    public SortedSet<Service> getAll() throws Exception {
        final SortedSet<Service> services = new TreeSet<>();
        for (final ServiceType serviceType : ServiceType.values()) {
            services.addAll(getAll(serviceType));
        }
        return services;
    }

    /**
     * @param serviceType the {@link ServiceType} indicating the type of services to retrieve
     * @return all of the available {@link Service} objects of the specified type that have registered with service
     * discovery
     */
    public SortedSet<Service> getAll(final ServiceType serviceType) throws Exception {
        final SortedSet<Service> services = new TreeSet<>();
        if (!isClosed()) {
            final List<ServiceInstance<String>> list =
                    new ArrayList<>(getDiscovery().queryForInstances(Objects.requireNonNull(serviceType).name()));
            list.stream().map(Service::new).forEach(services::add);
        }
        return services;
    }

    /**
     * @param serviceType the {@link ServiceType} indicating the type of service to retrieve
     * @return a randomly chosen {@link Service} of the specified type that has registered with service discovery,
     * possibly empty if there are no registered services of the specified type
     */
    public Optional<Service> getRandom(final ServiceType serviceType) throws Exception {
        if (!isClosed()) {
            final List<ServiceInstance<String>> services =
                    new ArrayList<>(getDiscovery().queryForInstances(Objects.requireNonNull(serviceType).name()));
            if (!services.isEmpty()) {
                // Return a random service instance from the list.
                return Optional.of(new Service(services.get(new Random().nextInt(services.size()))));
            }
        }
        return Optional.empty();
    }
}
