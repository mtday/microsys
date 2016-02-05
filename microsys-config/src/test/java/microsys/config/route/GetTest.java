package microsys.config.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.config.model.ConfigKeyValue;
import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Perform testing on the {@link Get} class.
 */
public class GetTest {
    @Test
    public void testNoKey() throws Exception {
        final Config config = Mockito.mock(Config.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);

        final Get get = new Get(config, configService);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(400);
        Mockito.verify(response).body("Invalid configuration key");
        assertNull(obj);
    }

    @Test
    public void testMissingKey() throws Exception {
        final Config config = Mockito.mock(Config.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);
        Mockito.when(configService.get(Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final Get get = new Get(config, configService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("key")).thenReturn("missing");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(404);
        Mockito.verify(response).body("Configuration key not found: missing");
        assertNull(obj);
    }

    @Test
    public void testWithResponse() throws Exception {
        final Config config = Mockito.mock(Config.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);
        Mockito.when(configService.get(Mockito.anyString()))
                .thenReturn(CompletableFuture.completedFuture(Optional.of(new ConfigKeyValue("key", "value"))));

        final Get get = new Get(config, configService);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("key")).thenReturn("key");
        final Response response = Mockito.mock(Response.class);

        final Object obj = get.handle(request, response);

        Mockito.verify(response).status(200);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals("{\"key\":\"key\",\"value\":\"value\"}", obj.toString());
    }
}
