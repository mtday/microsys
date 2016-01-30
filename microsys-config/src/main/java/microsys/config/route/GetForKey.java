package microsys.config.route;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import microsys.service.BaseRoute;
import spark.Request;
import spark.Response;

/**
 *
 */
public class GetForKey extends BaseRoute {
    /**
     * @param config the system configuration properties
     */
    public GetForKey(final Config config) {
        super(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) throws Exception {
        final String key = request.params(":key");

        if (getConfig().hasPath(key)) {
            final String value = getConfig().getString(key);

            response.status(200);
            response.type(MediaType.JSON_UTF_8.type());

            final JsonObject json = new JsonObject();
            json.addProperty("key", key);
            json.addProperty("value", value);
            return json;
        } else {
            response.status(404);
            response.body(String.format("Configuration key not found: %s", key));
            return null;
        }
    }
}
