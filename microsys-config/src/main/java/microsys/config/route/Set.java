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
 * Update (or create) a configuration value based on the user-provided data.
 */
public class Set extends BaseConfigRoute {
    /**
     * @param config the system configuration properties
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public Set(final Config config, final ConfigService configService) {
        super(config, configService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) throws Exception {
        final String key = request.params("key");
        final String value = request.queryParams("value");

        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            response.status(400);
            response.body("Invalid configuration key or value");
            return null;
        } else {
            final Optional<String> oldValue = getConfigService().set(key, value);

            response.status(200);
            response.type(MediaType.JSON_UTF_8.type());

            if (oldValue.isPresent()) {
                final JsonObject json = new JsonObject();
                json.addProperty("key", key);
                json.addProperty("value", oldValue.get());
                return json;
            } else {
                return new JsonObject();
            }
        }
    }
}
