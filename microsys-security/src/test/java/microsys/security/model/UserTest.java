package microsys.security.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Perform testing on the {@link User} class.
 */
public class UserTest {
    @Test
    public void testCompareTo() {
        final User a = new User("CN=user1", "user1", Arrays.asList("A", "B"));
        final User b = new User("CN=user1", "user1", Arrays.asList("B", "C"));
        final User c = new User("CN=user1", "user", Collections.singletonList("A"));
        final User d = new User("CN=user2", "user2", Arrays.asList("A", "B"));

        assertEquals(1, a.compareTo(null));

        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(1, a.compareTo(c));
        assertEquals(-1, a.compareTo(d));

        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(1, b.compareTo(c));
        assertEquals(-1, b.compareTo(d));

        assertEquals(-1, c.compareTo(a));
        assertEquals(-1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-1, c.compareTo(d));

        assertEquals(1, d.compareTo(a));
        assertEquals(1, d.compareTo(b));
        assertEquals(1, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
    }

    @Test
    public void testEquals() {
        final User a = new User("CN=user1", "user1", Arrays.asList("A", "B"));
        final User b = new User("CN=user1", "user1", Arrays.asList("B", "C"));
        final User c = new User("CN=user1", "user", Collections.singletonList("A"));
        final User d = new User("CN=user2", "user2", Arrays.asList("A", "B"));

        assertNotEquals(a, null);

        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);

        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(b, d);

        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
        assertNotEquals(c, d);

        assertNotEquals(d, a);
        assertNotEquals(d, b);
        assertNotEquals(d, c);
        assertEquals(d, d);
    }

    @Test
    public void testHashCode() {
        assertEquals(-1169232254, new User("CN=user1", "user1", Arrays.asList("A", "B")).hashCode());
    }

    @Test
    public void testToString() {
        final User user = new User("CN=user1", "user1", Arrays.asList("A", "B"));
        assertEquals("User[id=CN=user1,userName=user1,roles=[A, B]]", user.toString());
    }

    @Test
    public void testToJson() {
        final User user = new User("CN=user1", "user1", Arrays.asList("A", "B"));
        assertEquals("{\"id\":\"CN=user1\",\"userName\":\"user1\",\"roles\":[\"A\",\"B\"]}", user.toJson().toString());
    }

    @Test
    public void testJsonConstructor() {
        final User original = new User("CN=user1", "user1", Arrays.asList("A", "B"));
        final User copy = new User(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoId() {
        final String jsonStr = "{\"userName\":\"user1\",\"roles\":[\"A\",\"B\"]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new User(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorIdWrongType() {
        final String jsonStr = "{\"id\":[],\"userName\":\"user1\",\"roles\":[\"A\",\"B\"]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new User(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoUserName() {
        final String jsonStr = "{\"id\":\"CN=user1\",\"roles\":[\"A\",\"B\"]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new User(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorUserNameWrongType() {
        final String jsonStr = "{\"id\":\"CN=user1\",\"userName\":[],\"roles\":[\"A\",\"B\"]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new User(json);
    }

    @Test
    public void testJsonConstructorNoRoles() {
        final String jsonStr = "{\"id\":\"CN=user1\",\"userName\":\"user1\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        assertEquals(new User("CN=user1", "user1", Collections.emptyList()), new User(json));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorRolesWrongType() {
        final String jsonStr = "{\"id\":\"CN=user1\",\"userName\":\"user1\",\"roles\":\"AB\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new User(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorRoleValueWrongType() {
        final String jsonStr = "{\"id\":\"CN=user1\",\"userName\":\"user1\",\"roles\":[[],{\"a\":\"A\"}]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new User(json);
    }

    @Test
    public void testConverter() {
        final User.UserConverter converter = new User.UserConverter();
        final User original = new User("CN=user1", "user1", Arrays.asList("A", "B"));
        final JsonObject json = converter.doBackward(original);
        final User copy = converter.doForward(json);
        assertEquals(original, copy);

        assertEquals(converter, new User.UserConverter());
        assertEquals(452430762, converter.hashCode());
    }

    @Test
    public void testHasRole() {
        final User user = new User("CN=user1", "user1", Arrays.asList("A", "B"));
        assertTrue(user.hasRole("A"));
        assertTrue(user.hasRole("B"));
        assertFalse(user.hasRole("C"));
    }
}
