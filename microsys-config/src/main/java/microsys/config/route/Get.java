package microsys.config.route;

import com.google.common.net.MediaType;
import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;

import microsys.config.model.ConfigKeyValue;
import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Retrieves the configuration value for the provided key.
 */
public class Get extends BaseConfigRoute {
    /**
     * @param config the system configuration properties
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public Get(final Config config, final ConfigService configService) {
        super(config, configService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) throws Exception {
        final String key = request.params("key");

        if (StringUtils.isEmpty(key)) {
            response.status(400);
            response.body("Invalid configuration key");
            return null;
        } else {
            final Future<Optional<ConfigKeyValue>> future = getConfigService().get(key);
            final Optional<ConfigKeyValue> value = future.get(10, TimeUnit.SECONDS);

            if (value.isPresent()) {
                response.status(200);
                response.type(MediaType.JSON_UTF_8.type());
                return value.get().toJson();
            } else {
                response.status(404);
                response.body(String.format("Configuration key not found: %s", key));
                return null;
            }
        }
    }
}
