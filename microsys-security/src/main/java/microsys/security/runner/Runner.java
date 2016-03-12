package microsys.security.runner;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import microsys.common.model.service.ServiceType;
import microsys.security.route.GetById;
import microsys.security.route.GetByName;
import microsys.security.route.Remove;
import microsys.security.route.Save;
import microsys.security.service.UserService;
import microsys.security.service.impl.MemoryUserService;
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
        super(Objects.requireNonNull(config), ServiceType.SECURITY, serverStopLatch);
        addRoutes(new MemoryUserService());
    }

    protected void addRoutes(@Nonnull final UserService userService) {
        Spark.get("/id/:id", new GetById(getServiceEnvironment(), userService));
        Spark.get("/name/:name", new GetByName(getServiceEnvironment(), userService));
        Spark.post("/", new Save(getServiceEnvironment(), userService));
        Spark.delete("/:id", new Remove(getServiceEnvironment(), userService));
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
