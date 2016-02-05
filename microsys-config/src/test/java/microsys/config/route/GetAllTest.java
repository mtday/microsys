package microsys.config.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.TreeMap;

/**
 * Perform testing on the {@link GetAll} class.
 */
public class GetAllTest {
    @Test
    public void testWithResponse() throws Exception {
        final Map<String, String> data = new TreeMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");

        final Config config = Mockito.mock(Config.class);
        final ConfigService configService = Mockito.mock(ConfigService.class);
        Mockito.when(configService.getAll()).thenReturn(data);

        final GetAll getAll = new GetAll(config, configService);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);

        final Object obj = getAll.handle(request, response);

        Mockito.verify(response).status(200);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals(
                "{\"config\":[{\"key\":\"key1\",\"value\":\"value1\"},{\"key\":\"key2\",\"value\":\"value2\"}]}",
                obj.toString());
    }
}
