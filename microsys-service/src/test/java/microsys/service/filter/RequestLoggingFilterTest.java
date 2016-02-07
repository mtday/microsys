package microsys.service.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.service.BaseService;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

/**
 * Perform testing of the {@link BaseService} class.
 */
public class RequestLoggingFilterTest {
    @Test
    public void testRequestLoggingFilter() throws Exception {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestLoggingFilter.class)).setLevel(Level.OFF);

        final Map<String, String[]> parameterMap = new TreeMap<>();
        parameterMap.put("param1", new String[] {"val1", "val2"});
        parameterMap.put("param2", new String[] {"val3"});
        final HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(servletRequest.getParameterMap()).thenReturn(parameterMap);
        final QueryParamsMap queryParamsMap = new QueryParamsMap(servletRequest);
        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.requestMethod()).thenReturn("GET");
        Mockito.when(request.uri()).thenReturn("/uri");
        Mockito.when(request.queryMap()).thenReturn(queryParamsMap);

        final Response response = Mockito.mock(Response.class);
        final RequestLoggingFilter filter = new RequestLoggingFilter();

        filter.handle(request, response);
        assertEquals("param1 => [val1, val2], param2 => [val3]", filter.getParams(request));
        assertEquals("GET    /uri  param1 => [val1, val2], param2 => [val3]", filter.getMessage(request));
    }
}
