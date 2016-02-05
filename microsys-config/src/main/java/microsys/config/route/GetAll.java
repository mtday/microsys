package microsys.config.route;

import com.google.common.net.MediaType;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.typesafe.config.Config;

import microsys.config.service.ConfigService;
import spark.Request;
import spark.Response;

import java.util.Map;

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
        response.status(200);
        response.type(MediaType.JSON_UTF_8.type());

        final JsonArray jsonArr = new JsonArray();
        getConfigService().getAll().entrySet().stream().map(this::asJson).forEach(jsonArr::add);

        final JsonObject json = new JsonObject();
        json.add("config", jsonArr);
        return json;
    }

    protected JsonObject asJson(final Map.Entry<String, String> entry) {
        final JsonObject json = new JsonObject();
        json.addProperty("key", entry.getKey());
        json.addProperty("value", entry.getValue());
        return json;
    }
}
