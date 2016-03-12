package microsys.security.route;

import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import com.google.gson.JsonParser;

import microsys.security.model.User;
import microsys.security.service.UserService;
import microsys.service.model.ServiceEnvironment;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Update (or create) a {@link User} based on the provided data.
 */
public class Save extends BaseUserRoute {
    /**
     * @param serviceEnvironment the service environment
     * @param userService the {@link UserService} used to manage the user accounts in the system
     */
    public Save(@Nonnull final ServiceEnvironment serviceEnvironment, @Nonnull final UserService userService) {
        super(serviceEnvironment, userService);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Object handle(@Nonnull final Request request, @Nonnull final Response response)
            throws ExecutionException, InterruptedException, TimeoutException {
        try {
            final String body = request.body();
            Preconditions.checkArgument(body != null, "User object must be provided");

            final User user = new User(new JsonParser().parse(body).getAsJsonObject());
            final Future<Optional<User>> future = getUserService().save(user);
            final Optional<User> oldValue = future.get(10, TimeUnit.SECONDS);

            if (oldValue.isPresent()) {
                response.status(HttpServletResponse.SC_OK);
                response.type(MediaType.JSON_UTF_8.type());
                return oldValue.get().toJson();
            } else {
                response.status(HttpServletResponse.SC_NO_CONTENT);
                return getNoContent();
            }
        } catch (final IllegalArgumentException badInput) {
            response.status(HttpServletResponse.SC_BAD_REQUEST);
            return badInput.getMessage();
        }
    }
}
