package microsys.config.route;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;

import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.Optional;

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
            final Optional<String> value = getConfigService().get(key);

            if (value.isPresent()) {
                response.status(200);
                response.type(MediaType.JSON_UTF_8.type());

                final JsonObject json = new JsonObject();
                json.addProperty("key", key);
                json.addProperty("value", value.get());
                return json;
            } else {
                response.status(404);
                response.body(String.format("Configuration key not found: %s", key));
                return null;
            }
        }
    }
}
