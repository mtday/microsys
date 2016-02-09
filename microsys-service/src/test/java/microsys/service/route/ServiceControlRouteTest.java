package microsys.service.route;

import ch.qos.logback.classic.Level;
import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import microsys.service.BaseService;
import microsys.service.model.ServiceControlStatus;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Perform testing on the {@link ServiceControlRoute} class.
 */
public class ServiceControlRouteTest {
    @Test
    public void testStop() throws Exception {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ServiceControlRoute.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        final BaseService service = Mockito.mock(BaseService.class);
        Mockito.when(service.getConfig()).thenReturn(config);
        Mockito.when(service.getExecutor()).thenReturn(executor);

        final ServiceControlRoute route = Mockito.mock(ServiceControlRoute.class);
        Mockito.when(route.getService()).thenReturn(service);
        Mockito.doCallRealMethod().when(route).delayBeforeAction();
        Mockito.doCallRealMethod().when(route).stop(Mockito.anyBoolean());
        Mockito.doCallRealMethod().when(route).handle(Mockito.any(), Mockito.any());

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("action")).thenReturn("stop");
        final Response response = Mockito.mock(Response.class);
        final Object obj = route.handle(request, response);

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals(new ServiceControlStatus(true, "stop"), new ServiceControlStatus((JsonObject) obj));
    }

    @Test
    public void testStopInterrupted() throws Exception {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ServiceControlRoute.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        final BaseService service = Mockito.mock(BaseService.class);
        Mockito.when(service.getConfig()).thenReturn(config);
        Mockito.when(service.getExecutor()).thenReturn(executor);

        final ServiceControlRoute route = Mockito.mock(ServiceControlRoute.class);
        Mockito.when(route.getService()).thenReturn(service);
        Mockito.doThrow(new InterruptedException()).when(route).delayBeforeAction();
        Mockito.doCallRealMethod().when(route).stop(Mockito.anyBoolean());
        Mockito.doCallRealMethod().when(route).handle(Mockito.any(), Mockito.any());

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("action")).thenReturn("stop");
        final Response response = Mockito.mock(Response.class);
        final Object obj = route.handle(request, response);

        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals(new ServiceControlStatus(true, "stop"), new ServiceControlStatus((JsonObject) obj));
    }

    @Test
    public void testRestart() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ServiceControlRoute.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        final BaseService service = Mockito.mock(BaseService.class);
        Mockito.when(service.getConfig()).thenReturn(config);
        Mockito.when(service.getExecutor()).thenReturn(executor);

        final ServiceControlRoute route = new ServiceControlRoute(service);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("action")).thenReturn("restart");
        final Response response = Mockito.mock(Response.class);
        final Object obj = route.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_OK);
        Mockito.verify(response).type(MediaType.JSON_UTF_8.type());
        assertNotNull(obj);
        assertTrue(obj instanceof JsonObject);
        assertEquals(new ServiceControlStatus(true, "restart"), new ServiceControlStatus((JsonObject) obj));
    }

    @Test
    public void testUnrecognizedAction() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ServiceControlRoute.class)).setLevel(Level.OFF);

        final Config config = ConfigFactory.load();
        final ExecutorService executor = Executors.newFixedThreadPool(2);

        final BaseService service = Mockito.mock(BaseService.class);
        Mockito.when(service.getConfig()).thenReturn(config);
        Mockito.when(service.getExecutor()).thenReturn(executor);

        final ServiceControlRoute route = new ServiceControlRoute(service);

        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.params("action")).thenReturn("unrecognized");
        final Response response = Mockito.mock(Response.class);
        final Object obj = route.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("Unrecognized control action: unrecognized", obj);
    }

    @Test
    public void testNoParam() {
        final Config config = ConfigFactory.load();
        final BaseService service = Mockito.mock(BaseService.class);
        Mockito.when(service.getConfig()).thenReturn(config);
        final ServiceControlRoute route = new ServiceControlRoute(service);

        final Request request = Mockito.mock(Request.class);
        final Response response = Mockito.mock(Response.class);
        final Object obj = route.handle(request, response);

        Mockito.verify(response).status(HttpServletResponse.SC_BAD_REQUEST);
        assertEquals("A control action must be specified", obj);
    }
}
