package microsys.security.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import microsys.security.model.User;

import java.util.Arrays;
import java.util.Optional;

/**
 * Perform testing of the {@link MemoryUserService} class.
 */
public class MemoryUserServiceTest {
    @Test
    public void test() throws Exception {
        final MemoryUserService svc = new MemoryUserService();

        final Optional<User> byId = svc.getById("id").get();
        assertFalse(byId.isPresent());

        final Optional<User> byName = svc.getByName("name").get();
        assertFalse(byName.isPresent());

        final Optional<User> missing = svc.remove("id").get();
        assertFalse(missing.isPresent());

        final User user = new User("id", "name", Arrays.asList("A", "B"));
        final Optional<User> save = svc.save(user).get();
        assertFalse(save.isPresent());

        final Optional<User> byIdExists = svc.getById(user.getId()).get();
        assertTrue(byIdExists.isPresent());
        assertEquals(user, byIdExists.get());

        final Optional<User> byNameExists = svc.getByName(user.getUserName()).get();
        assertTrue(byNameExists.isPresent());
        assertEquals(user, byNameExists.get());

        final User updatedUser = new User(user.getId(), user.getUserName() + "2", Arrays.asList("A", "B", "C"));
        final Optional<User> save2 = svc.save(updatedUser).get();
        assertTrue(save2.isPresent());
        assertEquals(user, save2.get());

        final Optional<User> byIdUpdated = svc.getById(updatedUser.getId()).get();
        assertTrue(byIdUpdated.isPresent());
        assertEquals(updatedUser, byIdUpdated.get());

        final Optional<User> byNameUpdated = svc.getByName(updatedUser.getUserName()).get();
        assertTrue(byNameUpdated.isPresent());
        assertEquals(updatedUser, byNameUpdated.get());

        final Optional<User> removed = svc.remove(updatedUser.getId()).get();
        assertTrue(removed.isPresent());
        assertEquals(updatedUser, removed.get());
    }
}
