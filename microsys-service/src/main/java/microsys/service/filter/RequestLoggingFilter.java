package microsys.service.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Filter;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides a {@link Filter} implementation that logs request information.
 */
public class RequestLoggingFilter implements Filter {
    private final static Logger LOG = LoggerFactory.getLogger(RequestLoggingFilter.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(final Request request, final Response response) throws Exception {
        LOG.info(getMessage(Objects.requireNonNull(request)));
    }

    protected String getMessage(final Request request) {
        return String.format("%-6s %s  %s", request.requestMethod(), request.uri(), getParams(request));
    }

    protected String getParams(final Request request) {
        return String.join(
                ", ", request.queryMap().toMap().entrySet().stream()
                        .map(e -> String.format("%s => %s", e.getKey(), Arrays.asList(e.getValue()).toString()))
                        .collect(Collectors.toList()));
    }
}
