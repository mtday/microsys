package microsys.service.route;

import com.google.common.net.MediaType;
import com.typesafe.config.Config;

import microsys.common.config.CommonConfig;
import microsys.common.model.ServiceType;
import microsys.service.BaseRoute;
import microsys.service.model.ServiceInfo;
import spark.Request;
import spark.Response;

import java.util.Objects;

import javax.servlet.http.HttpServletResponse;

/**
 * A base route that provides some information about the service like the type and version.
 */
public class ServiceInfoRoute extends BaseRoute {
    private final ServiceType serviceType;

    /**
     * @param config the static system configuration information
     * @param serviceType the type of service for which this route has been deployed
     */
    public ServiceInfoRoute(final Config config, final ServiceType serviceType) {
        super(config);

        this.serviceType = Objects.requireNonNull(serviceType);
    }

    protected ServiceType getServiceType() {
        return this.serviceType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) {
        response.status(HttpServletResponse.SC_OK);
        response.type(MediaType.JSON_UTF_8.type());

        final String systemName = getConfig().getString(CommonConfig.SYSTEM_NAME.getKey());
        final String systemVersion = getConfig().getString(CommonConfig.SYSTEM_VERSION.getKey());

        return new ServiceInfo(getServiceType(), systemName, systemVersion).toJson();
    }
}
