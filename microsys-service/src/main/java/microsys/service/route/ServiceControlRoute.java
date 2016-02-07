package microsys.service.route;

import com.google.common.net.MediaType;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.service.BaseRoute;
import microsys.service.BaseService;
import spark.Request;
import spark.Response;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

/**
 * A base route that provides some REST end-points for controlling the service.
 */
public class ServiceControlRoute extends BaseRoute {
    private final static Logger LOG = LoggerFactory.getLogger(ServiceControlRoute.class);

    private final BaseService service;

    /**
     * @param service the {@link BaseService} providing access to the main service object that will be controlled
     */
    public ServiceControlRoute(final BaseService service) {
        super(Objects.requireNonNull(service).getConfig());
        this.service = service;
    }

    protected BaseService getService() {
        return this.service;
    }

    protected void delayBeforeAction() throws InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    }

    protected void stop(final boolean restart) {
        getService().getExecutor().submit(() -> {
            try {
                // Wait a little to allow for the response to make it back to the caller.
                LOG.info("Scheduling service {}", restart ? "restart" : "shutdown");
                delayBeforeAction();
                getService().setShouldRestart(restart);
                getService().stop();
            } catch (final InterruptedException interrupted) {
                // Ignored.
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) {
        final String action = request.params("action");

        if (StringUtils.isEmpty(action)) {
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            return "A control action must be specified";
        } else {
            response.status(HttpServletResponse.SC_OK);
            response.type(MediaType.JSON_UTF_8.type());

            if ("stop".equalsIgnoreCase(action)) {
                stop(false);
            } else if ("restart".equalsIgnoreCase(action)) {
                stop(true);
            } else {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                return "Unrecognized control action: " + action;
            }

            final JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.addProperty("action", action);
            return json;
        }
    }
}
