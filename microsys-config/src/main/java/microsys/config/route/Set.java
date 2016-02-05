package microsys.config.route;

import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;

import microsys.config.model.ConfigKeyValue;
import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
        try {
            final String body = request.body();
            Preconditions.checkArgument(body != null, "Configuration key and value must be provided");

            final ConfigKeyValue kv = new ConfigKeyValue(new JsonParser().parse(body).getAsJsonObject());
            final Future<Optional<ConfigKeyValue>> future = getConfigService().set(kv);
            final Optional<ConfigKeyValue> oldValue = future.get(10, TimeUnit.SECONDS);

            response.status(200);
            response.type(MediaType.JSON_UTF_8.type());
            if (oldValue.isPresent()) {
                return oldValue.get().toJson();
            }
            return new JsonObject();
        } catch (final IllegalArgumentException badInput) {
            response.status(400);
            response.body(badInput.getMessage());
            return null;
        }
    }
}
