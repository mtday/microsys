package microsys.config.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.config.model.ConfigKeyValue;
import microsys.config.model.ConfigKeyValueCollection;
import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing on the {@link GetAll} class.
 */
public class GetAllTest {
    @Test
    public void testWithResponse() throws Exception {
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value1");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value2");
        final ConfigKeyValueCollection coll = new ConfigKeyValueCollection(kv1, kv2);

        final Config config = Mockito.mock(Config.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);
        Mockito.when(configService.getAll()).thenReturn(CompletableFuture.completedFuture(coll));

        final GetAll getAll = new GetAll(config, configService);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);

        final Object obj = getAll.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals(
                "{\"config\":[{\"key\":\"key1\",\"value\":\"value1\"},{\"key\":\"key2\",\"value\":\"value2\"}]}",
                obj.toString());
    }
}
