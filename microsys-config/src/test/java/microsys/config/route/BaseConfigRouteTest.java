package microsys.config.route;

import static org.junit.Assert.assertEquals;

import com.typesafe.config.Config;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

/**
 * Perform testing on the {@link BaseConfigRoute} class.
 */
public class BaseConfigRouteTest {
    @Test
    public void test() {
        final Config config = Mockito.mock(Config.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);

        final BaseConfigRoute route = new BaseConfigRoute(config, configService) {
            @Override
            public Object handle(final Request request, final Response response) throws Exception {
                return null;
            }
        };

        assertEquals(config, route.getConfig());
        assertEquals(configService, route.getConfigService());
    }
}
