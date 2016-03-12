package microsys.config.route;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.config.service.ConfigService;
import microsys.service.model.ServiceEnvironment;
import spark.Request;
import spark.Response;

/**
 * Perform testing on the {@link BaseConfigRoute} class.
 */
public class BaseConfigRouteTest {
    @Test
    public void test() {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);

        final BaseConfigRoute route = new BaseConfigRoute(serviceEnvironment, configService) {
            @Override
            public Object handle(final Request request, final Response response) throws Exception {
                return null;
            }
        };

        assertEquals(serviceEnvironment, route.getServiceEnvironment());
        assertEquals(configService, route.getConfigService());
    }
}
