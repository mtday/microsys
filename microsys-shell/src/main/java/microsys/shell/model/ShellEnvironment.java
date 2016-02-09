package microsys.shell.model;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;

import microsys.service.client.ServiceClient;
import microsys.service.discovery.DiscoveryManager;
import microsys.shell.RegistrationManager;
import okhttp3.OkHttpClient;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Provides shell environmental configuration and utilities for use within commands.
 */
public class ShellEnvironment {
    private final Config config;
    private final ExecutorService executor;
    private final DiscoveryManager discoveryManager;
    private final CuratorFramework curatorFramework;
    private final RegistrationManager registrationManager;
    private final OkHttpClient httpClient;
    private final ServiceClient serviceClient;

    /**
     * @param config the static system configuration information
     * @param executor the {@link ExecutorService} used to perform asynchronous task processing
     * @param discoveryManager the service discovery manager used to find and manage available micro services
     * @param curatorFramework the curator framework used to manage interactions with zookeeper
     * @param registrationManager the manager used to track the available command registrations
     * @param httpClient the {@link OkHttpClient} used to make REST calls to other services
     */
    public ShellEnvironment(
            final Config config, final ExecutorService executor, final DiscoveryManager discoveryManager,
            final CuratorFramework curatorFramework, final RegistrationManager registrationManager,
            final OkHttpClient httpClient) {
        this.config = Objects.requireNonNull(config);
        this.executor = Objects.requireNonNull(executor);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.curatorFramework = Objects.requireNonNull(curatorFramework);
        this.registrationManager = Objects.requireNonNull(registrationManager);
        this.httpClient = Objects.requireNonNull(httpClient);

        this.serviceClient = new ServiceClient(executor, httpClient);
    }

    /**
     * @return the static system configuration information
     */
    protected Config getConfig() {
        return this.config;
    }

    /**
     * @return the {@link ExecutorService} used to perform asynchronous task processing
     */
    protected ExecutorService getExecutor() {
        return this.executor;
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

    /**
     * @return the {@link OkHttpClient} used to make REST calls to other services
     */
    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * @return the {@link ServiceClient} used to make remote service calls to other services
     */
    public ServiceClient getServiceClient() {
        return this.serviceClient;
    }

    /**
     * Close down the resources associated with this environment.
     */
    public void close() {
        getExecutor().shutdown();
        getCuratorFramework().close();
        getDiscoveryManager().close();
    }
}
