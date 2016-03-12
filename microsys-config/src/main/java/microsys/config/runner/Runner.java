package microsys.config.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import microsys.common.model.service.ServiceType;
import microsys.config.route.Get;
import microsys.config.route.GetAll;
import microsys.config.route.Set;
import microsys.config.route.Unset;
import microsys.config.service.ConfigService;
import microsys.config.service.impl.CuratorConfigService;
import microsys.service.BaseService;
import spark.Spark;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Nonnull;

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
    public Runner(@Nonnull final Config config, @Nonnull final CountDownLatch serverStopLatch) throws Exception {
        super(Objects.requireNonNull(config), ServiceType.CONFIG, serverStopLatch);
        addRoutes(new CuratorConfigService(getServiceEnvironment()));
    }

    protected void addRoutes(@Nonnull final ConfigService configService) {
        Spark.get("/", new GetAll(getServiceEnvironment(), configService));
        Spark.get("/:key", new Get(getServiceEnvironment(), configService));
        Spark.post("/", new Set(getServiceEnvironment(), configService));
        Spark.delete("/:key", new Unset(getServiceEnvironment(), configService));
    }

    /**
     * @param args the command-line parameters
     * @throws Exception if there is a problem during service initialization
     */
    public static void main(@Nonnull final String... args) throws Exception {
        boolean restart;
        do {
            final Config config = ConfigFactory.load().withFallback(ConfigFactory.systemProperties())
                    .withFallback(ConfigFactory.systemEnvironment());
            final CountDownLatch serverStopLatch = new CountDownLatch(1);
            final Runner runner = new Runner(config, serverStopLatch);
            serverStopLatch.await();

            restart = runner.getShouldRestart();
        } while (restart);
    }
}
