package microsys.security.runner;

import com.google.common.annotations.VisibleForTesting;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.curator.framework.CuratorFramework;

import microsys.common.model.ServiceType;
import microsys.security.route.GetById;
import microsys.security.route.GetByName;
import microsys.security.route.Remove;
import microsys.security.route.Save;
import microsys.security.service.MemoryUserService;
import microsys.security.service.UserService;
import microsys.service.BaseService;
import microsys.service.discovery.DiscoveryManager;
import spark.Spark;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
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
     * @param serverStopLatch the {@link CountDownLatch} used to manage the running server process
     * @throws Exception if there is a problem during service initialization
     */
    public Runner(final Config config, final CountDownLatch serverStopLatch) throws Exception {
        super(Objects.requireNonNull(config), ServiceType.SECURITY, serverStopLatch);
        addRoutes(new MemoryUserService());
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
        super(config, executor, curator, discoveryManager, ServiceType.SECURITY);
        addRoutes(new MemoryUserService());
    }

    protected void addRoutes(final UserService userService) {
        Spark.get("/id/:id", new GetById(getConfig(), userService));
        Spark.get("/name/:name", new GetByName(getConfig(), userService));
        Spark.post("/", new Save(getConfig(), userService));
        Spark.delete("/:id", new Remove(getConfig(), userService));
    }

    /**
     * @param args the command-line parameters
     * @throws Exception if there is a problem during service initialization
     */
    public static void main(final String... args) throws Exception {
        boolean restart;
        do {
            final CountDownLatch serverStopLatch = new CountDownLatch(1);
            final Runner runner = new Runner(ConfigFactory.load(), serverStopLatch);
            serverStopLatch.await();

            restart = runner.getShouldRestart();
        } while (restart);
    }
}
