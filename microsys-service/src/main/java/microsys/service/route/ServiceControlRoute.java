package microsys.service.route;

import com.google.common.net.MediaType;
import microsys.service.BaseRoute;
import microsys.service.BaseService;
import microsys.service.model.ServiceControlStatus;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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

    /**
     * @return the {@link BaseService} object to be managed
     */
    protected BaseService getService() {
        return this.service;
    }

    /**
     * Perform the delay, allowing time to return the response back to the caller.
     * @throws InterruptedException if the sleep operation is interrupted
     */
    protected void delayBeforeAction() throws InterruptedException {
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    }

    /**
     * Perform a stop, possibly followed by a start (if the {@code restart} parameter is true).
     *
     * @param restart whether the service should be restarted
     */
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

            return new ServiceControlStatus(true, action).toJson();
        }
    }
}
