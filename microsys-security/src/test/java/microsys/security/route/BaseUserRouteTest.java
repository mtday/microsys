package microsys.security.route;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.security.service.UserService;
import microsys.service.model.ServiceEnvironment;
import spark.Request;
import spark.Response;

/**
 * Perform testing on the {@link BaseUserRoute} class.
 */
public class BaseUserRouteTest {
    @Test
    public void test() {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);

        final BaseUserRoute route = new BaseUserRoute(serviceEnvironment, userService) {
            @Override
            public Object handle(final Request request, final Response response) throws Exception {
                return null;
            }
        };

        assertEquals(serviceEnvironment, route.getServiceEnvironment());
        assertEquals(userService, route.getUserService());
    }
}
