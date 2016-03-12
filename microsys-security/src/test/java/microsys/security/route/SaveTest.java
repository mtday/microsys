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
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing on the {@link Save} class.
 */
public class SaveTest {
    @Test
    public void testNoUser() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);

        final Save save = new Save(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);

        final Object obj = save.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("User object must be provided", obj);
    }

    @Test
    public void testNoId() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);

        final Save save = new Save(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.body()).thenReturn("{ \"userName\": \"userName\", \"roles\": [\"A\", \"B\"] }");
        final Response response = Mockito.mock(Response.class);

        final Object obj = save.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("ID field required", obj);
    }

    @Test
    public void testNoUserName() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);

        final Save save = new Save(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.body()).thenReturn("{ \"id\": \"id\", \"roles\": [\"A\", \"B\"] }");
        final Response response = Mockito.mock(Response.class);

        final Object obj = save.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("UserName field required", obj);
    }

    @Test
    public void testNoRoles() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.save(Mockito.any())).thenReturn(
                CompletableFuture.completedFuture(Optional.of(new User("id", "name", Collections.emptyList()))));

        final Save save = new Save(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.body()).thenReturn("{ \"id\": \"id\", \"userName\": \"name\" }");
        final Response response = Mockito.mock(Response.class);

        final Object obj = save.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals("{\"id\":\"id\",\"userName\":\"name\",\"roles\":[]}", obj.toString());
    }

    @Test
    public void testMissingKey() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.save(Mockito.any())).thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final Save save = new Save(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.body()).thenReturn(new User("id", "name", Arrays.asList("A", "B")).toJson().toString());
        final Response response = Mockito.mock(Response.class);

        final Object obj = save.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_NO_CONTENT);
        assertEquals("", obj);
    }

    @Test
    public void testWithResponse() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.save(Mockito.any())).thenReturn(
                CompletableFuture.completedFuture(Optional.of(new User("id", "name", Arrays.asList("A", "B")))));

        final Save save = new Save(serviceEnvironment, userService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.body()).thenReturn(new User("id", "name", Arrays.asList("A", "B")).toJson().toString());
        final Response response = Mockito.mock(Response.class);

        final Object obj = save.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals("{\"id\":\"id\",\"userName\":\"name\",\"roles\":[\"A\",\"B\"]}", obj.toString());
    }
}
