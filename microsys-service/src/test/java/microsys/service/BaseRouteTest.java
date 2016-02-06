package microsys.service;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;

import spark.Request;
import spark.Response;

/**
 * Perform testing on the {@link BaseRoute} class.
 */
public class BaseRouteTest {
    @Test
    public void test() {
        final Config config = ConfigFactory.load();

        final BaseRoute route = new BaseRoute(config) {
            @Override
            public Object handle(final Request request, final Response response) throws Exception {
                return null;
            }
        };

        assertEquals(config, route.getConfig());
    }
}
