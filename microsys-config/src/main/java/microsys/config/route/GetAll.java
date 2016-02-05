package microsys.config.route;

import com.google.common.net.MediaType;
import com.typesafe.config.Config;

import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

/**
 * Retrieve all of the dynamic system configuration properties managed in this system.
 */
public class GetAll extends BaseConfigRoute {
    /**
     * @param config the system configuration properties
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public GetAll(final Config config, final ConfigService configService) {
        super(config, configService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) throws Exception {
        response.status(HttpServletResponse.SC_OK);
        response.type(MediaType.JSON_UTF_8.type());

        return getConfigService().getAll().get(10, TimeUnit.SECONDS).toJson();
    }
}
