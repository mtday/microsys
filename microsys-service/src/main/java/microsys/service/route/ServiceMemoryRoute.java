package microsys.service.route;

import com.google.common.net.MediaType;

import microsys.service.BaseRoute;
import microsys.service.model.ServiceEnvironment;
import microsys.service.model.ServiceMemory;
import spark.Request;
import spark.Response;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * A base route that provides some information about the memory usage and availability in this service.
 */
public class ServiceMemoryRoute extends BaseRoute {
    /**
     * @param serviceEnvironment the service environment
     */
    public ServiceMemoryRoute(@Nonnull final ServiceEnvironment serviceEnvironment) {
        super(serviceEnvironment);
    }

    @Nonnull
    protected MemoryUsage getHeapMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
    }

    @Nonnull
    protected MemoryUsage getNonHeapMemoryUsage() {
        return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Object handle(@Nonnull final Request request, @Nonnull final Response response) {
        response.status(HttpServletResponse.SC_OK);
        response.type(MediaType.JSON_UTF_8.type());

        return new ServiceMemory(getHeapMemoryUsage(), getNonHeapMemoryUsage()).toJson();
    }
}
