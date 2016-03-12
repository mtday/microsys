package microsys.service.model;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;

import microsys.common.config.ConfigKeys;
import microsys.common.model.service.ServiceType;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.curator.CuratorCreator;
import microsys.curator.CuratorException;
import microsys.discovery.DiscoveryException;
import microsys.discovery.DiscoveryManager;
import microsys.discovery.impl.CuratorDiscoveryManager;
import okhttp3.OkHttpClient;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

/**
 * Provides service environmental configuration and utilities.
 */
public class ServiceEnvironment {
    @Nonnull
    private final Config config;
    @Nonnull
    private final ServiceType serviceType;
    @Nonnull
    private final ExecutorService executor;
    @Nonnull
    private final CryptoFactory cryptoFactory;
    @Nonnull
    private final CuratorFramework curatorFramework;
    @Nonnull
    private final DiscoveryManager discoveryManager;
    @Nonnull
    private final OkHttpClient httpClient;

    /**
     * @param config the static system configuration information
     * @param serviceType the type of this service
     */
    public ServiceEnvironment(
            @Nonnull final Config config, @Nonnull final ServiceType serviceType)
            throws DiscoveryException, CuratorException, EncryptionException {
        this.config = Objects.requireNonNull(config);
        this.serviceType = Objects.requireNonNull(serviceType);

        this.executor = createExecutor(this.config);
        this.cryptoFactory = createCryptoFactory(this.config);
        this.curatorFramework = CuratorCreator.create(this.config, this.cryptoFactory);
        this.discoveryManager = createDiscoveryManager(this.config, this.curatorFramework);
        this.httpClient = createHttpClient(this.cryptoFactory);
    }

    /**
     * @param config the static system configuration information
     * @param serviceType the type of this service
     * @param executor the {@link ExecutorService} used to perform asynchronous task processing
     * @param cryptoFactory the {@link CryptoFactory} used to perform encryption operations
     * @param curatorFramework the curator framework used to manage interactions with zookeeper
     * @param discoveryManager the service discovery manager used to find and manage available micro services
     * @param httpClient the {@link OkHttpClient} used to make REST calls to other services
     */
    public ServiceEnvironment(
            @Nonnull final Config config, @Nonnull final ServiceType serviceType,
            @Nonnull final ExecutorService executor, @Nonnull final CryptoFactory cryptoFactory,
            @Nonnull final CuratorFramework curatorFramework, @Nonnull final DiscoveryManager discoveryManager,
            @Nonnull final OkHttpClient httpClient) {
        this.config = Objects.requireNonNull(config);
        this.serviceType = Objects.requireNonNull(serviceType);
        this.executor = Objects.requireNonNull(executor);
        this.cryptoFactory = Objects.requireNonNull(cryptoFactory);
        this.curatorFramework = Objects.requireNonNull(curatorFramework);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    @Nonnull
    protected ExecutorService createExecutor(@Nonnull final Config config) {
        final int threads = Objects.requireNonNull(config).getInt(ConfigKeys.EXECUTOR_THREADS.getKey());
        return Executors.newFixedThreadPool(threads);
    }

    @Nonnull
    protected CryptoFactory createCryptoFactory(@Nonnull final Config config) throws DiscoveryException {
        return new DefaultCryptoFactory(Objects.requireNonNull(config));
    }

    @Nonnull
    protected DiscoveryManager createDiscoveryManager(
            @Nonnull final Config config, @Nonnull final CuratorFramework curator) throws DiscoveryException {
        return new CuratorDiscoveryManager(Objects.requireNonNull(config), Objects.requireNonNull(curator));
    }

    @Nonnull
    protected OkHttpClient createHttpClient(@Nonnull final CryptoFactory cryptoFactory) throws EncryptionException {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.sslSocketFactory(cryptoFactory.getSSLContext().getSocketFactory());
        return builder.build();
    }

    /**
     * @return the static system configuration information
     */
    @Nonnull
    public Config getConfig() {
        return this.config;
    }

    /**
     * @return the type of service being hosted
     */
    @Nonnull
    public ServiceType getServiceType() {
        return this.serviceType;
    }

    /**
     * @return the {@link ExecutorService} used to perform asynchronous task processing
     */
    @Nonnull
    public ExecutorService getExecutor() {
        return this.executor;
    }

    /**
     * @return the service discovery manager used to find and manage available micro services
     */
    @Nonnull
    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    /**
     * @return the curator framework used to manage interactions with zookeeper
     */
    @Nonnull
    public CuratorFramework getCuratorFramework() {
        return this.curatorFramework;
    }

    /**
     * @return the {@link OkHttpClient} used to make REST calls to other services
     */
    @Nonnull
    public OkHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * @return the {@link CryptoFactory} used to perform encryption operations
     */
    @Nonnull
    public CryptoFactory getCryptoFactory() {
        return this.cryptoFactory;
    }

    /**
     * Close down the resources associated with this environment.
     */
    public void close() {
        getExecutor().shutdown();
        getCuratorFramework().close();
        try {
            getDiscoveryManager().close();
        } catch (final DiscoveryException closeFailed) {
            // Ignored.
        }
    }
}
