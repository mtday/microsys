package microsys.security.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.security.model.User;
import microsys.security.service.UserService;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing on the {@link GetById} class.
 */
public class GetByIdTest {
    @Test
    public void testNoKey() throws Exception {
        final Config config = Mockito.mock(Config.class);
        final UserService userService = Mockito.mock(UserService.class);

        final GetById get = new GetById(config, userService);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("Invalid request, a user id must be provided", obj);
    }

    @Test
    public void testMissingKey() throws Exception {
        final Config config = Mockito.mock(Config.class);
        final UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getById(Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final GetById get = new GetById(config, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("id")).thenReturn("missing");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("A user with the specified id was not found: missing", obj);
    }

    @Test
    public void testWithResponse() throws Exception {
        final Config config = Mockito.mock(Config.class);
        final UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getById(Mockito.anyString())).thenReturn(
                CompletableFuture.completedFuture(Optional.of(new User("id", "name", Arrays.asList("A", "B")))));

        final GetById get = new GetById(config, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("id")).thenReturn("id");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals("{\"id\":\"id\",\"userName\":\"name\",\"roles\":[\"A\",\"B\"]}", obj.toString());
    }
}
