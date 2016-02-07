package microsys.service;

import com.typesafe.config.Config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import microsys.service.discovery.DiscoveryManager;
import microsys.service.discovery.port.PortManager;
import microsys.service.filter.RequestLoggingFilter;
import microsys.service.model.Reservation;
import microsys.service.model.Service;
import microsys.service.route.ServiceInfoRoute;
import microsys.service.route.ServiceMemoryRoute;
import spark.Spark;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The base class for a micro service in this system.
 */
public abstract class BaseService {
    private final static Logger LOG = LoggerFactory.getLogger(BaseService.class);

    private final Config config;
    private final ExecutorService executor;
    private final CuratorFramework curator;
    private final DiscoveryManager discoveryManager;
    private final ServiceType serviceType;

    private Optional<Service> service;

    /**
     * @param config the static system configuration information
     * @param serviceType the type of this service
     */
    public BaseService(final Config config, final ServiceType serviceType) throws Exception {
        this.config = Objects.requireNonNull(config);
        this.serviceType = Objects.requireNonNull(serviceType);

        this.executor = createExecutor(config);
        this.curator = createCurator(config);
        this.discoveryManager = createDiscoveryManager(this.config, this.curator);

        start();
    }

    /**
     * @param config the static system configuration information
     * @param executor the {@link ExecutorService} used to perform asynchronous task processing
     * @param curator the {@link CuratorFramework} used to perform communication with zookeeper
     * @param discoveryManager the {@link DiscoveryManager} used to manage available services
     * @param serviceType the type of this service
     */
    public BaseService(
            final Config config, final ExecutorService executor, final CuratorFramework curator,
            final DiscoveryManager discoveryManager, final ServiceType serviceType) throws Exception {
        this.config = Objects.requireNonNull(config);
        this.executor = Objects.requireNonNull(executor);
        this.curator = Objects.requireNonNull(curator);
        this.discoveryManager = Objects.requireNonNull(discoveryManager);
        this.serviceType = Objects.requireNonNull(serviceType);

        start();
    }

    /**
     * @return the static system configuration information
     */
    public Config getConfig() {
        return this.config;
    }

    /**
     * @return the {@link ExecutorService} used to perform asynchronous task processing
     */
    public ExecutorService getExecutor() {
        return this.executor;
    }

    /**
     * @return the {@link CuratorFramework} used to perform communication with zookeeper
     */
    public CuratorFramework getCurator() {
        return this.curator;
    }

    /**
     * @return the {@link DiscoveryManager} used to manage available services
     */
    public DiscoveryManager getDiscoveryManager() {
        return this.discoveryManager;
    }

    /**
     * @return the type of service being hosted
     */
    public ServiceType getServiceType() {
        return this.serviceType;
    }

    /**
     * @return the {@link Service} describing this running service, possibly not present if not running
     */
    public Optional<Service> getService() {
        return this.service;
    }

    protected String getHostName() {
        return getConfig().getString(CommonConfig.SERVER_HOSTNAME.getKey());
    }

    protected static DiscoveryManager createDiscoveryManager(final Config config, final CuratorFramework curator)
            throws Exception {
        return new DiscoveryManager(Objects.requireNonNull(config), Objects.requireNonNull(curator));
    }

    protected static ExecutorService createExecutor(final Config config) {
        return Executors.newFixedThreadPool(config.getInt(CommonConfig.EXECUTOR_THREADS.getKey()));
    }

    protected static CuratorFramework createCurator(final Config config) throws Exception {
        final String zookeepers = config.getString(CommonConfig.ZOOKEEPER_HOSTS.getKey());
        final String namespace = config.getString(CommonConfig.SYSTEM_NAME.getKey());
        final CuratorFramework curator =
                CuratorFrameworkFactory.builder().connectString(zookeepers).namespace(namespace)
                        .defaultData(new byte[0]).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        if (!curator.blockUntilConnected(1500, TimeUnit.MILLISECONDS)) {
            throw new Exception("Failed to connect to zookeeper");
        }
        return curator;
    }

    protected void configurePort(final Reservation reservation) {
        Spark.port(reservation.getPort());
    }

    protected void configureThreading() {
        final int maxThreads = getConfig().getInt(CommonConfig.SERVER_THREADS_MAX.getKey());
        final int minThreads = getConfig().getInt(CommonConfig.SERVER_THREADS_MIN.getKey());
        final long timeout = getConfig().getDuration(CommonConfig.SERVER_TIMEOUT.getKey(), TimeUnit.MILLISECONDS);

        Spark.threadPool(maxThreads, minThreads, (int) timeout);
    }

    protected void configureSecurity() {
        final boolean ssl = getConfig().getBoolean(CommonConfig.SSL_ENABLED.getKey());
        if (ssl) {
            final String keystoreFile = getConfig().getString(CommonConfig.SSL_KEYSTORE_FILE.getKey());
            final String keystorePass = getConfig().getString(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey());
            final String truststoreFile = getConfig().getString(CommonConfig.SSL_TRUSTSTORE_FILE.getKey());
            final String truststorePass = getConfig().getString(CommonConfig.SSL_TRUSTSTORE_PASSWORD.getKey());

            Spark.secure(keystoreFile, keystorePass, truststoreFile, truststorePass);
        }
    }

    protected void configureRequestLogger() {
        Spark.before(new RequestLoggingFilter());
    }

    protected void configureRoutes() {
        Spark.get("/service/info", new ServiceInfoRoute(getConfig(), getServiceType()));
        Spark.get("/service/memory", new ServiceMemoryRoute(getConfig()));
    }

    /**
     * Start the service.
     *
     * @throws Exception if there is a problem starting the service
     */
    public void start() throws Exception {
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
        this.service = Optional.of(new Service(getServiceType(), reservation.getHost(), reservation.getPort(), ssl,
                version));

        // Register with service discovery once the server has started.
        this.executor.submit(() -> {
            Spark.awaitInitialization();
            LOG.info("Service {} started on {}:{}", getServiceType(), reservation.getHost(), reservation.getPort());

            try {
                if (getService().isPresent()) {
                    getDiscoveryManager().register(getService().get());
                }
            } catch (final Exception registerFailed) {
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
        } catch (final Exception unregisterFailed) {
            // Not really an issue because the ephemeral registration will disappear automatically soon.
            LOG.warn("Failed to unregister with service discovery", unregisterFailed);
        }

        Spark.stop();
        getDiscoveryManager().close();
        getCurator().close();
    }
}
