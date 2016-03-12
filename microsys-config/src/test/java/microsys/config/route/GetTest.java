package microsys.config.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.config.model.ConfigKeyValue;
import microsys.config.service.ConfigService;
import microsys.service.model.ServiceEnvironment;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing on the {@link Get} class.
 */
public class GetTest {
    @Test
    public void testNoKey() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);

        final Get get = new Get(serviceEnvironment, configService);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("Invalid configuration key", obj);
    }

    @Test
    public void testMissingKey() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);
        Mockito.when(configService.get(Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final Get get = new Get(serviceEnvironment, configService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("key")).thenReturn("missing");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_NOT_FOUND);
        assertEquals("Configuration key not found: missing", obj);
    }

    @Test
    public void testWithResponse() throws Exception {
        final ServiceEnvironment serviceEnvironment = Mockito.mock(ServiceEnvironment.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);
        Mockito.when(configService.get(Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new ConfigKeyValue("key", "value"))));

        final Get get = new Get(serviceEnvironment, configService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("key")).thenReturn("key");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals("{\"key\":\"key\",\"value\":\"value\"}", obj.toString());
    }
}
