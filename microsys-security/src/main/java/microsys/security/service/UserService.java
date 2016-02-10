package microsys.security.service;

import microsys.security.model.User;

import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Defines the capabilities required of {@link UserService} implementations, which manage {@link User} accounts in
 * this system.
 */
public interface UserService {
    /**
     * @param id the unique identifier of the user account to retrieve
     * @return an {@link Optional} of the requested {@link User}, empty if the user was not found and wrapped in a
     * {@link Future}
     */
    Future<Optional<User>> getById(String id);

    /**
     * @param userName the user name of the user account to retrieve
     * @return an {@link Optional} of the requested {@link User}, empty if the user was not found and wrapped in a
     * {@link Future}
     */
    Future<Optional<User>> getByName(String userName);

    /**
     * @param user the {@link User} object to be updated
     * @return the original {@link User} object prior to the update, empty if the user did not exist and wrapped in a
     * {@link Future}
     */
    Future<Optional<User>> save(User user);

    /**
     * @param id the unique identifier fo the user account to remove
     * @return the original {@link User} object prior to the removal, empty if the user did not exist and wrapped in a
     * {@link Future}
     */
    Future<Optional<User>> remove(String id);
}
