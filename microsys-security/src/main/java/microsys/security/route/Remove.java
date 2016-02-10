package microsys.security.route;

import com.google.common.net.MediaType;
import com.typesafe.config.Config;

import org.apache.commons.lang3.StringUtils;

import microsys.security.model.User;
import microsys.security.service.UserService;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletResponse;

/**
 * Removes the {@link User} based on the provided user id.
 */
public class Remove extends BaseUserRoute {
    /**
     * @param config the static system configuration properties
     * @param userService the {@link UserService} used to manage the system user accounts
     */
    public Remove(final Config config, final UserService userService) {
        super(config, userService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object handle(final Request request, final Response response) throws Exception {
        final String id = request.params("id");

        if (StringUtils.isEmpty(id)) {
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            return "Invalid request, a user id must be provided";
        } else {
            final Future<Optional<User>> future = getUserService().remove(id);
            final Optional<User> value = future.get(10, TimeUnit.SECONDS);

            if (value.isPresent()) {
                response.status(HttpServletResponse.SC_OK);
                response.type(MediaType.JSON_UTF_8.type());
                return value.get().toJson();
            } else {
                response.status(HttpServletResponse.SC_NO_CONTENT);
                return NO_CONTENT;
            }
        }
    }
}
