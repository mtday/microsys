package microsys.service.route;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.Test;
import org.mockito.Mockito;

import microsys.service.model.ServiceMemory;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;

/**
 * Perform testing on the {@link ServiceMemoryRoute} class.
 */
public class ServiceMemoryRouteTest {
    @Test
    public void testHandle() {
        final Config config = ConfigFactory.load();
        final ServiceMemoryRoute route = new ServiceMemoryRoute(config);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);
        final Object obj = route.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertNotNull(new ServiceMemory((JsonObject) obj));
    }
}
