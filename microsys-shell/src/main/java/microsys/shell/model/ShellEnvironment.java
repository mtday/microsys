package microsys.shell.model;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;

import microsys.common.model.service.ServiceType;
import microsys.config.client.ConfigClient;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.curator.CuratorException;
import microsys.discovery.DiscoveryException;
import microsys.discovery.DiscoveryManager;
import microsys.service.client.ServiceClient;
import microsys.service.model.ServiceEnvironment;
import microsys.shell.RegistrationManager;
import okhttp3.OkHttpClient;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nonnull;

/**
 * Provides shell environmental configuration and utilities for use within commands.
 */
public class ShellEnvironment extends ServiceEnvironment {
    @Nonnull
    private final RegistrationManager registrationManager;

    /**
     * @param config the static system configuration information
     */
    public ShellEnvironment(@Nonnull final Config config)
            throws DiscoveryException, CuratorException, EncryptionException {
        super(config, ServiceType.SHELL);
        this.registrationManager = new RegistrationManager();
        this.registrationManager.loadCommands(this);
    }

    /**
     * @param config the static system configuration information
     * @param executor the {@link ExecutorService} used to perform asynchronous task processing
     * @param discoveryManager the service discovery manager used to find and manage available micro services
     * @param curatorFramework the curator framework used to manage interactions with zookeeper
     * @param registrationManager the manager used to track the available command registrations
     * @param httpClient the {@link OkHttpClient} used to make REST calls to other services
     * @param cryptoFactory the {@link CryptoFactory} used to perform encryption operations
     */
    public ShellEnvironment(
            @Nonnull final Config config, @Nonnull final ExecutorService executor,
            @Nonnull final CryptoFactory cryptoFactory, @Nonnull final CuratorFramework curatorFramework,
            @Nonnull final DiscoveryManager discoveryManager, @Nonnull final OkHttpClient httpClient,
            @Nonnull final RegistrationManager registrationManager) {
        super(config, ServiceType.SHELL, executor, cryptoFactory, curatorFramework, discoveryManager, httpClient);
        this.registrationManager = Objects.requireNonNull(registrationManager);
    }

    /**
     * @return the manager used to track the available command registrations
     */
    @Nonnull
    public RegistrationManager getRegistrationManager() {
        return this.registrationManager;
    }

    /**
     * @return a {@link ServiceClient} used to make remote service calls to other services
     */
    @Nonnull
    public ServiceClient getServiceClient() {
        return new ServiceClient(this);
    }

    /**
     * @return a {@link ConfigClient} used to make remote calls to the dynamic configuration services
     */
    @Nonnull
    public ConfigClient getConfigClient() {
        return new ConfigClient(this);
    }
}
