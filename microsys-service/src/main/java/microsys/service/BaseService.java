package microsys.service;

import com.typesafe.config.Config;

import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.common.config.CommonConfig;
import microsys.common.curator.CuratorACLProvider;
import microsys.common.model.ServiceType;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.service.discovery.DiscoveryException;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.discovery.port.PortManager;
import microsys.service.discovery.port.PortReservationException;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.model.Reservation;
import microsys.service.model.Service;
import microsys.service.route.ServiceControlRoute;
import microsys.service.route.ServiceInfoRoute;
import microsys.service.route.ServiceMemoryRoute;
import spark.Spark;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

/**
 * The base class for a micro service in this system.
 */
public abstract class BaseService {
    private final static Logger LOG = LoggerFactory.getLogger(BaseService.class);

    @Nonnull
    private final Config config;
    @Nonnull
    private final ServiceType serviceType;
    @Nonnull
    private final Optional<CountDownLatch> serverStopLatch;

    @Nonnull
    private final ExecutorService executor;
    @Nonnull
    private final CuratorFramework curator;
    @Nonnull
    private final DiscoveryManager discoveryManager;
    @Nonnull
    private final CryptoFactory cryptoFactory;

    @Nonnull
    private Optional<Service> service;
    private boolean shouldRestart = false;

    /**
     * @param config          the static system configuration information
     * @param serviceType     the type of this service
     * @param serverStopLatch the {@link CountDownLatch} used to manage the running server process
     */
    public BaseService(
            @Nonnull final Config config, @Nonnull final ServiceType serviceType,
            @Nonnull final CountDownLatch serverStopLatch) throws Exception {
        this.config = Objects.requireNonNull(config);
        this.serviceType = Objects.requireNonNull(serviceType);
        this.serverStopLatch = Optional.of(Objects.requireNonNull(serverStopLatch));

        this.cryptoFactory = createCryptoFactory(this.config);
        this.executor = createExecutor(config);
        this.curator = createCurator(config, cryptoFactory);
        this.discoveryManager = createDiscoveryManager(this.config, this.curator);

        this.service = Optional.empty();

        start();
    }

    /**
     * @param config           the static system configuration information
     * @param executor         the {@link ExecutorService} used to perform asynchronous task processing
     * @param curator          the {@link CuratorFramework} used to perform communication with zookeeper
     * @param discoveryManager the {@link DiscoveryManager} used to manage available services
     * @param cryptoFactory    the {@link CryptoFactory} used to manage encryption and decryption operations
     * @param serviceType      the type of this service
     */
    public BaseService(
            @Nonnull final Config config, @Nonnull final ExecutorService executor,
            @Nonnull final CuratorFramework curator, @Nonnull final DiscoveryManager discoveryManager,
            @Nonnull final CryptoFactory cryptoFactory, @Nonnull final ServiceType serviceType) throws Exception {
        this.config = Objects.requireNonNull(config);
        this.executor = Objects.requireNonNull(executor);
        this.curator = Objects.requireNonNull(curator);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.cryptoFactory = Objects.requireNonNull(cryptoFactory);
        this.serviceType = Objects.requireNonNull(serviceType);

        this.serverStopLatch = Optional.empty();
        this.service = Optional.empty();

        start();
    }

    /**
     * @return the static system configuration information
     */
    @Nonnull
    public Config getConfig() {
        return this.config;
    }

    /**
     * @return the {@link ExecutorService} used to perform asynchronous task processing
     */
    @Nonnull
    public ExecutorService getExecutor() {
        return this.executor;
    }

    /**
     * @return the {@link CuratorFramework} used to perform communication with zookeeper
     */
    @Nonnull
    public CuratorFramework getCurator() {
        return this.curator;
    }

    /**
     * @return the {@link DiscoveryManager} used to manage available services
     */
    @Nonnull
    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    /**
     * @return the {@link CryptoFactory} used to manage encryption and decryption operations
     */
    @Nonnull
    public CryptoFactory getCryptoFactory() {
        return this.cryptoFactory;
    }

    /**
     * @return the type of service being hosted
     */
    @Nonnull
    public ServiceType getServiceType() {
        return this.serviceType;
    }

    /**
     * @return the {@link CountDownLatch} tracking the running server process
     */
    @Nonnull
    public Optional<CountDownLatch> getServerStopLatch() {
        return this.serverStopLatch;
    }

    /**
     * @return the {@link Service} describing this running service, possibly not present if not running
     */
    @Nonnull
    public Optional<Service> getService() {
        return this.service;
    }

    /**
     * @return whether the service should be restarted
     */
    public boolean getShouldRestart() {
        return this.shouldRestart;
    }

    /**
     * @param shouldRestart whether the service should be restarted
     */
    public void setShouldRestart(final boolean shouldRestart) {
        this.shouldRestart = shouldRestart;
    }

    @Nonnull
    protected String getHostName() {
        return getConfig().getString(CommonConfig.SERVER_HOSTNAME.getKey());
    }

    @Nonnull
    protected DiscoveryManager createDiscoveryManager(
            @Nonnull final Config config, @Nonnull final CuratorFramework curator) throws DiscoveryException {
        return new DiscoveryManager(Objects.requireNonNull(config), Objects.requireNonNull(curator));
    }

    @Nonnull
    protected ExecutorService createExecutor(@Nonnull final Config config) {
        return Executors.newFixedThreadPool(config.getInt(CommonConfig.EXECUTOR_THREADS.getKey()));
    }

    @Nonnull
    protected CuratorFramework createCurator(@Nonnull final Config config, @Nonnull final CryptoFactory cryptoFactory)
            throws InterruptedException, TimeoutException, EncryptionException {
        Objects.requireNonNull(config);
        Objects.requireNonNull(cryptoFactory);

        final String zookeepers = config.getString(CommonConfig.ZOOKEEPER_HOSTS.getKey());
        final boolean secure = config.getBoolean(CommonConfig.ZOOKEEPER_AUTH_ENABLED.getKey());
        final String namespace = config.getString(CommonConfig.SYSTEM_NAME.getKey());
        final CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString(zookeepers);
        builder.namespace(namespace);
        builder.retryPolicy(new ExponentialBackoffRetry(1000, 3));
        builder.defaultData(new byte[0]);
        if (secure) {
            final String user = config.getString(CommonConfig.ZOOKEEPER_AUTH_USER.getKey());
            final String pass = cryptoFactory.getDecryptedConfig(CommonConfig.ZOOKEEPER_AUTH_PASSWORD.getKey());
            final byte[] authData = String.format("%s:%s", user, pass).getBytes(StandardCharsets.UTF_8);
            final AuthInfo authInfo = new AuthInfo("digest", authData);
            builder.authorization(Collections.singletonList(authInfo));
            builder.aclProvider(new CuratorACLProvider());
        }
        final CuratorFramework curator = builder.build();
        curator.start();
        if (!curator.blockUntilConnected(2, TimeUnit.SECONDS)) {
            throw new TimeoutException("Failed to connect to zookeeper");
        }
        return curator;
    }

    @Nonnull
    protected CryptoFactory createCryptoFactory(@Nonnull final Config config) throws DiscoveryException {
        return new CryptoFactory(Objects.requireNonNull(config));
    }

    protected void configurePort(@Nonnull final Reservation reservation) {
        Spark.port(reservation.getPort());
    }

    protected void configureThreading() {
        final int maxThreads = getConfig().getInt(CommonConfig.SERVER_THREADS_MAX.getKey());
        final int minThreads = getConfig().getInt(CommonConfig.SERVER_THREADS_MIN.getKey());
        final long timeout = getConfig().getDuration(CommonConfig.SERVER_TIMEOUT.getKey(), TimeUnit.MILLISECONDS);

        Spark.threadPool(maxThreads, minThreads, (int) timeout);
    }

    protected void configureSecurity() throws EncryptionException {
        final boolean ssl = getConfig().getBoolean(CommonConfig.SSL_ENABLED.getKey());
        if (ssl) {
            final String keystoreFile = getConfig().getString(CommonConfig.SSL_KEYSTORE_FILE.getKey());
            final String keystorePass =
                    getCryptoFactory().getDecryptedConfig(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey());
            final String truststoreFile = getConfig().getString(CommonConfig.SSL_TRUSTSTORE_FILE.getKey());
            final String truststorePass =
                    getCryptoFactory().getDecryptedConfig(CommonConfig.SSL_TRUSTSTORE_PASSWORD.getKey());
            Spark.secure(keystoreFile, keystorePass, truststoreFile, truststorePass);
        }
    }

    protected void configureRequestLogger() {
        Spark.before(new RequestLoggingFilter());
    }

    protected void configureRoutes() {
        Spark.get("/service/info", new ServiceInfoRoute(getConfig(), getServiceType()));
        Spark.get("/service/memory", new ServiceMemoryRoute(getConfig()));
        Spark.get("/service/control/:action", new ServiceControlRoute(this));
    }

    /**
     * Start the service.
     * @throws PortReservationException if there is a problem reserving the port for the service
     * @throws EncryptionException if there is a problem decrypting the SSL key store or trust store passwords
     */
    public void start() throws PortReservationException, EncryptionException {
        // Configure the service.
        final PortManager portManager = new PortManager(getConfig(), getCurator());
        final Reservation reservation = portManager.getReservation(getServiceType(), getHostName());
        portManager.close();

        configurePort(reservation);
        configureThreading();
        configureSecurity();
        configureRequestLogger();
        configureRoutes();

        final boolean ssl = getConfig().getBoolean(CommonConfig.SSL_ENABLED.getKey());
        final String version = getConfig().getString(CommonConfig.SYSTEM_VERSION.getKey());
        this.service =
                Optional.of(new Service(getServiceType(), reservation.getHost(), reservation.getPort(), ssl, version));

        // Register with service discovery once the server has started.
        this.executor.submit(() -> {
            Spark.awaitInitialization();
            LOG.info("Service {} started on {}:{}", getServiceType(), reservation.getHost(), reservation.getPort());

            try {
                if (getService().isPresent()) {
                    getDiscoveryManager().register(getService().get());
                }
            } catch (final DiscoveryException registerFailed) {
                LOG.error("Failed to register with service discovery", registerFailed);
                stop();
            }
        });
    }

    /**
     * Stop the service.
     */
    public void stop() {
        try {
            if (getService().isPresent()) {
                getDiscoveryManager().unregister(getService().get());
                this.service = Optional.empty();
            }
        } catch (final DiscoveryException unregisterFailed) {
            // Not really an issue because the ephemeral registration will disappear automatically soon.
            LOG.warn("Failed to unregister with service discovery", unregisterFailed);
        }

        getDiscoveryManager().close();
        getCurator().close();
        getExecutor().shutdown();
        Spark.stop();

        if (getServerStopLatch().isPresent()) {
            getServerStopLatch().get().countDown();
        }
    }
}
