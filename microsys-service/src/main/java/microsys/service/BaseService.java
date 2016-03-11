package microsys.service;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.common.config.ConfigKeys;
import microsys.common.model.service.Reservation;
import microsys.common.model.service.Service;
import microsys.common.model.service.ServiceType;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.curator.CuratorCreator;
import microsys.discovery.DiscoveryException;
import microsys.discovery.DiscoveryManager;
import microsys.discovery.impl.CuratorDiscoveryManager;
import microsys.portres.PortManager;
import microsys.portres.PortReservationException;
import microsys.portres.impl.CuratorPortManager;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.filter.RequestSigningFilter;
import microsys.service.route.ServiceControlRoute;
import microsys.service.route.ServiceInfoRoute;
import microsys.service.route.ServiceMemoryRoute;
import spark.Spark;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        this.curator = CuratorCreator.create(config, this.cryptoFactory);
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
        return getConfig().getString(ConfigKeys.SERVER_HOSTNAME.getKey());
    }

    @Nonnull
    protected DiscoveryManager createDiscoveryManager(
            @Nonnull final Config config, @Nonnull final CuratorFramework curator) throws DiscoveryException {
        return new CuratorDiscoveryManager(Objects.requireNonNull(config), Objects.requireNonNull(curator));
    }

    @Nonnull
    protected ExecutorService createExecutor(@Nonnull final Config config) {
        return Executors.newFixedThreadPool(config.getInt(ConfigKeys.EXECUTOR_THREADS.getKey()));
    }

    @Nonnull
    protected CryptoFactory createCryptoFactory(@Nonnull final Config config) throws DiscoveryException {
        return new DefaultCryptoFactory(Objects.requireNonNull(config));
    }

    protected void configurePort(@Nonnull final Reservation reservation) {
        Spark.port(reservation.getPort());
    }

    protected void configureThreading() {
        final int maxThreads = getConfig().getInt(ConfigKeys.SERVER_THREADS_MAX.getKey());
        final int minThreads = getConfig().getInt(ConfigKeys.SERVER_THREADS_MIN.getKey());
        final long timeout = getConfig().getDuration(ConfigKeys.SERVER_TIMEOUT.getKey(), TimeUnit.MILLISECONDS);

        Spark.threadPool(maxThreads, minThreads, (int) timeout);
    }

    protected void configureSecurity() throws EncryptionException {
        final boolean ssl = getConfig().getBoolean(ConfigKeys.SSL_ENABLED.getKey());
        if (ssl) {
            final String keystoreFile = getConfig().getString(ConfigKeys.SSL_KEYSTORE_FILE.getKey());
            final String keystorePass =
                    getCryptoFactory().getDecryptedConfig(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey());
            final String truststoreFile = getConfig().getString(ConfigKeys.SSL_TRUSTSTORE_FILE.getKey());
            final String truststorePass =
                    getCryptoFactory().getDecryptedConfig(ConfigKeys.SSL_TRUSTSTORE_PASSWORD.getKey());
            Spark.secure(keystoreFile, keystorePass, truststoreFile, truststorePass);
        }
    }

    protected void configureRequestLogger() {
        Spark.before(new RequestLoggingFilter());
    }

    protected void configureRequestSigner(@Nonnull final Config config, @Nonnull final CryptoFactory cryptoFactory) {
        Spark.before(new RequestSigningFilter(config, cryptoFactory));
    }

    protected void configureRoutes() {
        Spark.get("/service/info", new ServiceInfoRoute(getConfig(), getServiceType()));
        Spark.get("/service/memory", new ServiceMemoryRoute(getConfig()));
        Spark.get("/service/control/:action", new ServiceControlRoute(this));
    }

    protected Reservation getPortReservation() throws PortReservationException {
        try (final PortManager portManager = new CuratorPortManager(getConfig(), getCurator())) {
            return portManager.getReservation(getServiceType(), getHostName());
        }
    }

    /**
     * Start the service.
     * @throws PortReservationException if there is a problem reserving the port for the service
     * @throws EncryptionException if there is a problem decrypting the SSL key store or trust store passwords
     */
    public void start() throws PortReservationException, EncryptionException {
        // Configure the service.
        final Reservation reservation = getPortReservation();

        configurePort(reservation);
        configureThreading();
        configureSecurity();
        configureRequestLogger();
        configureRequestSigner(getConfig(), getCryptoFactory());
        configureRoutes();

        final boolean ssl = getConfig().getBoolean(ConfigKeys.SSL_ENABLED.getKey());
        final String version = getConfig().getString(ConfigKeys.SYSTEM_VERSION.getKey());
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
            getDiscoveryManager().close();
        } catch (final DiscoveryException failure) {
            // Not really an issue because the ephemeral registration will disappear automatically soon.
            LOG.warn("Failed to unregister with service discovery", failure);
        }

        getCurator().close();
        getExecutor().shutdown();
        Spark.stop();

        if (getServerStopLatch().isPresent()) {
            getServerStopLatch().get().countDown();
        }
    }
}
