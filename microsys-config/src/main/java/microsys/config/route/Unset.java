package microsys.config.route;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
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
 * Unset and remove a configuration value based on the user-provided key.
 */
public class Unset extends BaseConfigRoute {
    /**
     * @param config the system configuration properties
     * @param configService the {@link ConfigService} used to manage the dynamic system configuration properties
     */
    public Unset(final Config config, final ConfigService configService) {
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
            final Future<Optional<ConfigKeyValue>> future = getConfigService().unset(key);
            final Optional<ConfigKeyValue> oldValue = future.get(10, TimeUnit.SECONDS);

            response.status(200);
            response.type(MediaType.JSON_UTF_8.type());
            if (oldValue.isPresent()) {
                return oldValue.get().toJson();
            }
            return new JsonObject();
        }
    }
}
