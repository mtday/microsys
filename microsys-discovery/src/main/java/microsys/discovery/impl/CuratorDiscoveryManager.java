package microsys.discovery.impl;

import com.google.gson.JsonParser;
import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;

import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.discovery.DiscoveryException;
import microsys.discovery.DiscoveryManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;

/**
 * The class is used to manage the system service discovery activity and is responsible for registering and
 * unregistering services, and retrieving available services.
 */
public class CuratorDiscoveryManager implements DiscoveryManager {
    @Nonnull
    private final Config config;
    @Nonnull
    private final ServiceDiscovery<String> discovery;

    private boolean isClosed = false;

    /**
     * @param config the static system configuration information
     * @param curator the {@link CuratorFramework} that is managing zookeeper operations
     * @throws DiscoveryException if there is a problem starting the service discovery service
     */
    public CuratorDiscoveryManager(@Nonnull final Config config, @Nonnull final CuratorFramework curator)
            throws DiscoveryException {
        this.config = Objects.requireNonNull(config);
        this.discovery = ServiceDiscoveryBuilder.builder(String.class).client(curator).basePath("/discovery").build();
        try {
            this.discovery.start();
        } catch (final Exception exception) {
            throw new DiscoveryException("Failed to start service discovery", exception);
        }
    }

    /**
     * @return the static system configuration information
     */
    @Nonnull
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the internal curator {@link ServiceDiscovery} object that manages the interaction with zookeeper
     */
    @Nonnull
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
     * @param service the {@link ServiceInstance} as stored and managed by zookeeper
     */
    @Nonnull
    protected Service createService(@Nonnull final ServiceInstance<String> service) {
        Objects.requireNonNull(service);
        return new Service(new JsonParser().parse(String.valueOf(service.getPayload())).getAsJsonObject());
    }

    /**
     * @return the {@link ServiceInstance} object used to represent this service when stored in zookeeper for service
     * discovery
     */
    @Nonnull
    protected ServiceInstance<String> createServiceInstance(@Nonnull final Service service) {
        Objects.requireNonNull(service);
        return new ServiceInstance<>(service.getType().name(), service.getId(), service.getHost(), service.getPort(),
                null, service.toJson().toString(), new Date().getTime(),
                org.apache.curator.x.discovery.ServiceType.DYNAMIC, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        try {
            this.isClosed = true;
            getDiscovery().close();
        } catch (final IOException ignored) {
            // Ignored.
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(@Nonnull final Service service) throws DiscoveryException {
        final ServiceInstance<String> serviceInstance = createServiceInstance(Objects.requireNonNull(service));
        if (!isClosed()) {
            try {
                getDiscovery().registerService(serviceInstance);
            } catch (final Exception exception) {
                throw new DiscoveryException("Failed to register service " + service, exception);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister(@Nonnull final Service service) throws DiscoveryException {
        final ServiceInstance<String> serviceInstance = createServiceInstance(Objects.requireNonNull(service));
        if (!isClosed()) {
            try {
                getDiscovery().unregisterService(serviceInstance);
            } catch (final Exception exception) {
                throw new DiscoveryException("Failed to unregister service " + service, exception);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SortedSet<Service> getAll() throws DiscoveryException {
        final SortedSet<Service> services = new TreeSet<>();
        for (final ServiceType serviceType : ServiceType.values()) {
            services.addAll(getAll(serviceType));
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public SortedSet<Service> getAll(@Nonnull final ServiceType serviceType) throws DiscoveryException {
        final SortedSet<Service> services = new TreeSet<>();
        if (!isClosed()) {
            try {
                final List<ServiceInstance<String>> list =
                        new ArrayList<>(getDiscovery().queryForInstances(Objects.requireNonNull(serviceType).name()));
                list.stream().map(this::createService).forEach(services::add);
            } catch (final Exception exception) {
                throw new DiscoveryException("Failed to retrieve service services of type " + serviceType, exception);
            }
        }
        return services;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Optional<Service> getRandom(@Nonnull final ServiceType serviceType) throws DiscoveryException {
        if (!isClosed()) {
            try {
                final List<ServiceInstance<String>> services =
                        new ArrayList<>(getDiscovery().queryForInstances(Objects.requireNonNull(serviceType).name()));
                if (!services.isEmpty()) {
                    // Return a random service instance from the list.
                    return Optional.of(createService(services.get(new Random().nextInt(services.size()))));
                }
            } catch (final Exception exception) {
                throw new DiscoveryException("Failed to retrieve random service of type " + serviceType, exception);
            }
        }
        return Optional.empty();
    }
}
