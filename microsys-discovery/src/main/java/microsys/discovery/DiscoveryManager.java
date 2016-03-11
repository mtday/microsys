package microsys.discovery;


import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;

import java.util.Optional;
import java.util.SortedSet;

import javax.annotation.Nonnull;

/**
 * The interface defines the requirements for the system service discovery capabilities.
 */
public interface DiscoveryManager {
    /**
     * Shutdown the resources associated with this service discovery manager.
     * @throws DiscoveryException if there is a problem cleaning up the discovery manager
     */
    void close() throws DiscoveryException;

    /**
     * @param service the {@link Service} to register
     * @throws DiscoveryException if there is a problem registering the service
     */
    void register(@Nonnull Service service) throws DiscoveryException;

    /**
     * @param service the {@link Service} to unregister
     * @throws DiscoveryException if there is a problem unregistering the service
     */
    void unregister(@Nonnull Service service) throws DiscoveryException;

    /**
     * @return all of the available {@link Service} objects that have registered with service discovery
     * @throws DiscoveryException if there is a problem retrieving all of the discoverable services
     */
    @Nonnull
    SortedSet<Service> getAll() throws DiscoveryException;

    /**
     * @param serviceType the {@link ServiceType} indicating the type of services to retrieve
     * @return all of the available {@link Service} objects of the specified type that have registered with service
     * discovery
     * @throws DiscoveryException if there is a problem retrieving the discoverable services of the specified type
     */
    @Nonnull
    SortedSet<Service> getAll(@Nonnull ServiceType serviceType) throws DiscoveryException;

    /**
     * @param serviceType the {@link ServiceType} indicating the type of service to retrieve
     * @return a randomly chosen {@link Service} of the specified type that has registered with service discovery,
     * possibly empty if there are no registered services of the specified type
     * @throws DiscoveryException if there is a problem retrieving a random discoverable service of the specified type
     */
    @Nonnull
    Optional<Service> getRandom(@Nonnull ServiceType serviceType) throws DiscoveryException;
}
