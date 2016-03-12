package microsys.security.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.security.model.User;
import microsys.security.service.UserService;
import microsys.service.model.ServiceEnvironment;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing on the {@link GetByName} class.
 */
public class GetByNameTest {
    @Test
    public void testNoKey() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);

        final GetByName get = new GetByName(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("Invalid request, a user name must be provided", obj);
    }

    @Test
    public void testMissingKey() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getByName(Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final GetByName get = new GetByName(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("name")).thenReturn("missing");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("A user with the specified name was not found: missing", obj);
    }

    @Test
    public void testWithResponse() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getByName(Mockito.anyString())).thenReturn(
                CompletableFuture.completedFuture(Optional.of(new User("id", "name", Arrays.asList("A", "B")))));

        final GetByName get = new GetByName(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("name")).thenReturn("name");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals("{\"id\":\"id\",\"userName\":\"name\",\"roles\":[\"A\",\"B\"]}", obj.toString());
    }
}
