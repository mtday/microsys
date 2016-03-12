package microsys.service.route;

import com.google.common.net.MediaType;

import microsys.common.config.ConfigKeys;
import microsys.service.BaseRoute;
import microsys.service.model.ServiceEnvironment;
import microsys.service.model.ServiceInfo;
import spark.Request;
import spark.Response;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * A base route that provides some information about the service like the type and version.
 */
public class ServiceInfoRoute extends BaseRoute {
    /**
     * @param serviceEnvironment the service environment
     */
    public ServiceInfoRoute(@Nonnull final ServiceEnvironment serviceEnvironment) {
        super(serviceEnvironment);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Object handle(@Nonnull final Request request, @Nonnull final Response response) {
        response.status(HttpServletResponse.SC_OK);
        response.type(MediaType.JSON_UTF_8.type());

        final String systemName = getServiceEnvironment().getConfig().getString(ConfigKeys.SYSTEM_NAME.getKey());
        final String systemVersion = getServiceEnvironment().getConfig().getString(ConfigKeys.SYSTEM_VERSION.getKey());
        return new ServiceInfo(getServiceEnvironment().getServiceType(), systemName, systemVersion).toJson();
    }
}
