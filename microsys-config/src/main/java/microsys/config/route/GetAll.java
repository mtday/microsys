package microsys.config.route;

import com.google.common.net.MediaType;

import microsys.config.service.ConfigService;
import microsys.service.model.ServiceEnvironment;
import spark.Request;
import spark.Response;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Retrieve all of the dynamic system configuration properties managed in this system.
 */
public class GetAll extends BaseConfigRoute {
    /**
     * @param serviceEnvironment the service environment
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public GetAll(@Nonnull final ServiceEnvironment serviceEnvironment, @Nonnull final ConfigService configService) {
        super(serviceEnvironment, configService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Object handle(@Nonnull final Request request, @Nonnull final Response response)
            throws ExecutionException, InterruptedException, TimeoutException {
        response.status(HttpServletResponse.SC_OK);
        response.type(MediaType.JSON_UTF_8.type());

        return getConfigService().getAll().get(10, TimeUnit.SECONDS).toJson();
    }
}
