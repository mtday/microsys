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
public class GetAll extends BaseRoute {
    /**
     * @param config the system configuration properties
     */
    public GetAll(final Config config) {
        super(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) throws Exception {
        response.status(200);
        response.type(MediaType.JSON_UTF_8.type());

        final JsonObject json = new JsonObject();
        getConfig().entrySet().forEach(e -> json.addProperty(e.getKey(), e.getValue().render()));
        return json;
    }
}
