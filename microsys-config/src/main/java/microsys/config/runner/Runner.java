package microsys.config.runner;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.curator.framework.CuratorFramework;

import microsys.common.model.ServiceType;
import microsys.config.route.Get;
import microsys.config.route.GetAll;
import microsys.config.route.Set;
import microsys.config.route.Unset;
import microsys.config.service.ConfigService;
import microsys.config.service.CuratorConfigService;
import microsys.service.BaseService;
import microsys.service.discovery.DiscoveryManager;
import spark.Spark;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * The main class used to run this service.
 */
public class Runner extends BaseService {
    /**
     * This constructor initializes the system configuration, then configures the REST end-points and starts the
     * micro service
     *
     * @param config the static system configuration information
     * @throws Exception if there is a problem during service initialization
     */
    public Runner(final Config config) throws Exception {
        super(Objects.requireNonNull(config), ServiceType.CONFIG);
        addRoutes(new CuratorConfigService(getExecutor(), getCurator()));
    }

    /**
     * This constructor initializes the system configuration, then configures the REST end-points and starts the
     * micro service
     *
     * @param config the static system configuration information
     * @param executor the {@link ExecutorService} used to process asynchronous tasks
     * @param curator the {@link CuratorFramework} used to perform communication with zookeeper
     * @param discoveryManager the {@link DiscoveryManager} used to manage available services
     * @throws Exception if there is a problem during service initialization
     */
    @VisibleForTesting
    public Runner(final Config config, final ExecutorService executor, final CuratorFramework curator, final
            DiscoveryManager discoveryManager) throws Exception {
        super(config, executor, curator, discoveryManager, ServiceType.CONFIG);
        addRoutes(new CuratorConfigService(getExecutor(), getCurator()));
    }

    protected void addRoutes(final ConfigService configService) {
        Spark.get("/", new GetAll(getConfig(), configService));
        Spark.get("/:key", new Get(getConfig(), configService));
        Spark.post("/", new Set(getConfig(), configService));
        Spark.delete("/:key", new Unset(getConfig(), configService));
    }

    /**
     * @param args the command-line parameters
     * @throws Exception if there is a problem during service initialization
     */
    public static void main(final String... args) throws Exception {
        new Runner(ConfigFactory.load());
    }
}
