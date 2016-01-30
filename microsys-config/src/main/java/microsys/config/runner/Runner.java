package microsys.config.runner;

import com.typesafe.config.ConfigFactory;

import microsys.common.model.ServiceType;
import microsys.config.route.Add;
import microsys.config.route.GetAll;
import microsys.config.route.GetForKey;
import microsys.service.BaseService;
import spark.Spark;

/**
 * The main class used to run this service.
 */
public class Runner extends BaseService {
    /**
     * This default constructor initializes the system configuration, then configures the REST end-points and starts
     * the micro service
     *
     * @throws Exception if there is a problem during service initialization
     */
    public Runner() throws Exception {
        super(ConfigFactory.load(), ServiceType.CONFIG);

        addRoutes();
    }

    protected void addRoutes() {
        Spark.get("/", new GetAll(getConfig()));
        Spark.get("/:key", new GetForKey(getConfig()));
        Spark.post("/", new Add(getConfig()));
    }

    /**
     * @param args the command-line parameters
     * @throws Exception if there is a problem during service initialization
     */
    public static void main(final String... args) throws Exception {
        new Runner();
    }
}
