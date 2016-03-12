package microsys.service;

import com.google.common.annotations.VisibleForTesting;
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
import microsys.discovery.DiscoveryException;
import microsys.discovery.DiscoveryManager;
import microsys.portres.PortManager;
import microsys.portres.PortReservationException;
import microsys.portres.impl.CuratorPortManager;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.filter.RequestSigningFilter;
import microsys.service.model.ServiceEnvironment;
import microsys.service.route.ServiceControlRoute;
import microsys.service.route.ServiceInfoRoute;
import microsys.service.route.ServiceMemoryRoute;
import okhttp3.OkHttpClient;
import spark.Spark;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * The base class for a micro service in this system.
 */
public abstract class BaseService {
    private final static Logger LOG = LoggerFactory.getLogger(BaseService.class);

    @Nonnull
    private final ServiceEnvironment serviceEnvironment;

    @Nonnull
    private final Optional<CountDownLatch> serverStopLatch;
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
        this.serviceEnvironment = new ServiceEnvironment(config, serviceType);
        this.serverStopLatch = Optional.of(Objects.requireNonNull(serverStopLatch));
        this.service = Optional.empty();

        start();
    }

    /**
     * @param config           the static system configuration information
     * @param serviceType      the type of this service
     * @param executor         the {@link ExecutorService} used to perform asynchronous task processing
     * @param cryptoFactory    the {@link CryptoFactory} used to manage encryption and decryption operations
     * @param curator          the {@link CuratorFramework} used to perform communication with zookeeper
     * @param discoveryManager the {@link DiscoveryManager} used to manage available services
     * @param httpClient       the {@link OkHttpClient} used to make REST calls to other services
     */
    @VisibleForTesting
    public BaseService(
            @Nonnull final Config config, @Nonnull final ServiceType serviceType,
            @Nonnull final ExecutorService executor, @Nonnull final CryptoFactory cryptoFactory,
            @Nonnull final CuratorFramework curator, @Nonnull final DiscoveryManager discoveryManager,
            @Nonnull final OkHttpClient httpClient) throws Exception {
        this.serviceEnvironment =
                new ServiceEnvironment(config, serviceType, executor, cryptoFactory, curator, discoveryManager,
                        httpClient);
        this.serverStopLatch = Optional.empty();
        this.service = Optional.empty();

        start();
    }

    /**
     * @return the environment objects available to the service
     */
    @Nonnull
    public ServiceEnvironment getServiceEnvironment() {
        return this.serviceEnvironment;
    }

    /**
     * @return the {@link CountDownLatch} tracking the running server process
     */
    @Nonnull
    protected Optional<CountDownLatch> getServerStopLatch() {
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
     * @return whether the service should be restarted after being shut down
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
        return getServiceEnvironment().getConfig().getString(ConfigKeys.SERVER_HOSTNAME.getKey());
    }

    protected void configurePort(@Nonnull final Reservation reservation) {
        Spark.port(reservation.getPort());
    }

    protected void configureThreading() {
        final int maxThreads = getServiceEnvironment().getConfig().getInt(ConfigKeys.SERVER_THREADS_MAX.getKey());
        final int minThreads = getServiceEnvironment().getConfig().getInt(ConfigKeys.SERVER_THREADS_MIN.getKey());
        final long timeoutMillis = getServiceEnvironment().getConfig()
                .getDuration(ConfigKeys.SERVER_TIMEOUT.getKey(), TimeUnit.MILLISECONDS);

        Spark.threadPool(maxThreads, minThreads, (int) timeoutMillis);
    }

    protected void configureSecurity() throws EncryptionException {
        final boolean ssl = getServiceEnvironment().getConfig().getBoolean(ConfigKeys.SSL_ENABLED.getKey());
        if (ssl) {
            final String keystoreFile =
                    getServiceEnvironment().getConfig().getString(ConfigKeys.SSL_KEYSTORE_FILE.getKey());
            final String keystorePass = getServiceEnvironment().getCryptoFactory()
                    .getDecryptedConfig(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey());
            final String truststoreFile =
                    getServiceEnvironment().getConfig().getString(ConfigKeys.SSL_TRUSTSTORE_FILE.getKey());
            final String truststorePass = getServiceEnvironment().getCryptoFactory()
                    .getDecryptedConfig(ConfigKeys.SSL_TRUSTSTORE_PASSWORD.getKey());
            Spark.secure(keystoreFile, keystorePass, truststoreFile, truststorePass);
        }
    }

    protected void configureRequestLogger() {
        Spark.before(new RequestLoggingFilter());
    }

    protected void configureRequestSigner() {
        Spark.before(new RequestSigningFilter(getServiceEnvironment()));
    }

    protected void configureRoutes() {
        Spark.get("/service/info", new ServiceInfoRoute(getServiceEnvironment()));
        Spark.get("/service/memory", new ServiceMemoryRoute(getServiceEnvironment()));
        Spark.get("/service/control/:action", new ServiceControlRoute(this));
    }

    protected Reservation getPortReservation() throws PortReservationException {
        try (final PortManager portManager = new CuratorPortManager(
                getServiceEnvironment().getConfig(), getServiceEnvironment().getCuratorFramework())) {
            return portManager.getReservation(getServiceEnvironment().getServiceType(), getHostName());
        }
    }

    protected void registerWithServiceDiscovery(final Reservation reservation) {
        final ServiceType serviceType = getServiceEnvironment().getServiceType();
        final boolean ssl = getServiceEnvironment().getConfig().getBoolean(ConfigKeys.SSL_ENABLED.getKey());
        final String version = getServiceEnvironment().getConfig().getString(ConfigKeys.SYSTEM_VERSION.getKey());
        this.service =
                Optional.of(new Service(serviceType, reservation.getHost(), reservation.getPort(), ssl, version));

        // Register with service discovery once the server has started.
        getServiceEnvironment().getExecutor().submit(() -> {
            Spark.awaitInitialization();
            LOG.info("Service {} started on {}:{}", serviceType, reservation.getHost(), reservation.getPort());

            try {
                if (getService().isPresent()) {
                    getServiceEnvironment().getDiscoveryManager().register(getService().get());
                }
            } catch (final DiscoveryException registerFailed) {
                LOG.error("Failed to register with service discovery", registerFailed);
                stop();
            }
        });
    }

    protected void unregisterWithServiceDiscovery() {
        try {
            if (getService().isPresent()) {
                getServiceEnvironment().getDiscoveryManager().unregister(getService().get());
                this.service = Optional.empty();
            }
        } catch (final DiscoveryException failure) {
            // Not really an issue because the ephemeral registration will disappear automatically soon.
            LOG.warn("Failed to unregister with service discovery", failure);
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
        configureRequestSigner();
        configureRoutes();

        registerWithServiceDiscovery(reservation);
    }

    /**
     * Stop the service.
     */
    public void stop() {
        unregisterWithServiceDiscovery();

        Spark.stop();
        getServiceEnvironment().close();

        if (getServerStopLatch().isPresent()) {
            getServerStopLatch().get().countDown();
        }
    }
}
