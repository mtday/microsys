package microsys.service;

import static org.junit.Assert.assertNotNull;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.service.model.ServiceEnvironment;
import spark.Request;
import spark.Response;

/**
 * Perform testing on the {@link BaseRoute} class.
 */
public class BaseRouteTest {
    @Test
    public void test() {
        final Config config = ConfigFactory.load();
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        Mockito.when(serviceEnvironment.getConfig()).thenReturn(config);

        final BaseRoute route = new BaseRoute(serviceEnvironment) {
            @Override
            public Object handle(final Request request, final Response response) throws Exception {
                return null;
            }
        };

        assertNotNull(route.getServiceEnvironment());
    }
}
