package microsys.service.route;

import com.google.common.net.MediaType;
import com.typesafe.config.Config;

import microsys.service.BaseRoute;
import microsys.service.model.ServiceMemory;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import javax.servlet.http.HttpServletResponse;

/**
 * A base route that provides some information about the memory usage and availability in this service.
 */
public class ServiceMemoryRoute extends BaseRoute {
    /**
     * @param config the static system configuration information
     */
    public ServiceMemoryRoute(final Config config) {
        super(config);
    }

    protected MemoryUsage getHeapMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    }

    protected MemoryUsage getNonHeapMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) {
        response.status(HttpServletResponse.SC_OK);
        response.type(MediaType.JSON_UTF_8.type());

        return new ServiceMemory(getHeapMemoryUsage(), getNonHeapMemoryUsage()).toJson();
    }
}
