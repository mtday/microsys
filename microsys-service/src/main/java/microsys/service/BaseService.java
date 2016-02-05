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
import microsys.service.discovery.PortManager;
import microsys.service.model.Reservation;
import microsys.service.model.Service;
import spark.Spark;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The base class for a micro service in this system.
 */
public abstract class BaseService {
    private final static Logger LOG = LoggerFactory.getLogger(BaseService.class);

    private final Config config;
    private final ExecutorService executor;
    private final CuratorFramework curator;
    private final DiscoveryManager discoveryManager;

    /**
     * @param config the static system configuration information
     * @param type the type of this service
     */
    public BaseService(final Config config, final ServiceType type) throws Exception {
        this.config = Objects.requireNonNull(config);
        this.executor = Executors.newFixedThreadPool(this.config.getInt(CommonConfig.EXECUTOR_THREADS.getKey()));
        this.curator = createCurator();

        // Configure the service.
        final Reservation reservation = new PortManager(getConfig(), curator).reserveServicePort(type, getHostName());
        configurePort(reservation);
        configureThreading();
        configureSecurity();
        configureRequestLogger();
        configureCompression();

        this.discoveryManager = new DiscoveryManager(getConfig(), curator);
        final boolean ssl = getConfig().getBoolean(CommonConfig.SSL_ENABLED.getKey());
        final Service service = new Service(type, reservation.getHost(), reservation.getPort(), ssl);

        // Register with service discovery once the server has started.
        new Thread(() -> {
            Spark.awaitInitialization();
            LOG.info("Service {} started on {}:{}", type, reservation.getHost(), reservation.getPort());

            try {
                getDiscoveryManager().register(service);
            } catch (final Exception registerFailed) {
                LOG.error("Failed to register with service discovery", registerFailed);
                Spark.stop();
            }
        }).start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                getDiscoveryManager().unregister(service);
            } catch (final Exception unregisterFailed) {
                LOG.error("Failed to unregister with service discovery", unregisterFailed);
            }
        }));
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

    protected String getHostName() {
        return getConfig().getString(CommonConfig.SERVER_HOSTNAME.getKey());
    }

    protected CuratorFramework createCurator() throws Exception {
        final String zookeepers = getConfig().getString(CommonConfig.ZOOKEEPER_HOSTS.getKey());
        final String namespace = getConfig().getString(CommonConfig.SYSTEM_NAME.getKey());
        final CuratorFramework curator =
                CuratorFrameworkFactory.builder().connectString(zookeepers).namespace(namespace)
                        .defaultData(new byte[0]).retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();
        curator.start();
        if (!curator.blockUntilConnected(5, TimeUnit.SECONDS)) {
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

    protected void configureCompression() {
        Spark.after((request, response) -> response.header("Content-Encoding", "gzip"));
    }

    protected void configureRequestLogger() {
        Spark.before((request, response) -> {
            final String params = String.join(
                    ", ", request.queryMap().toMap().entrySet().stream()
                            .map(e -> String.format("%s => %s", e.getKey(), Arrays.asList(e.getValue()).toString()))
                            .collect(Collectors.toList()));

            LOG.info(String.format("%-6s %s  %s", request.requestMethod(), request.uri(), params));
        });
    }

    /**
     * Stop the service.
     */
    public void stop() {
        Spark.stop();
        this.discoveryManager.close();
        this.curator.close();
    }
}
