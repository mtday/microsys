package microsys.service.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing on the {@link ServiceInfoRoute} class.
 */
public class ServiceInfoRouteTest {
    @Test
    public void test() {
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.SYSTEM_NAME.getKey(), ConfigValueFactory.fromAnyRef("system-name"));
        map.put(CommonConfig.SYSTEM_VERSION.getKey(), ConfigValueFactory.fromAnyRef("1.2.3"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final ServiceInfoRoute route = new ServiceInfoRoute(config, ServiceType.CONFIG);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);
        final Object obj = route.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals(
                "{\"type\":\"CONFIG\",\"systemName\":\"system-name\",\"systemVersion\":\"1.2.3\"}", obj.toString());
    }
}
