package microsys.config.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.Arrays;

/**
 * Perform testing on the {@link ConfigPath} class.
 */
public class ConfigPathTest {
    @Test
    public void testFromEnum() {
        final ConfigPath path = new ConfigPath(TestEnum.TEST_CONFIG_PROPERTY1);
        assertEquals(Arrays.asList("test", "config", "property1").toString(), path.getPath().toString());
    }

    @Test
    public void testFromJson() {
        final ConfigPath original = new ConfigPath(TestEnum.TEST_CONFIG_PROPERTY1);
        final ConfigPath copy = new ConfigPath(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonNoPath() {
        final JsonObject json = new JsonParser().parse("{ }").getAsJsonObject();
        new ConfigPath(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonPathNotArray() {
        final JsonObject json = new JsonParser().parse("{ path: 5 }").getAsJsonObject();
        new ConfigPath(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonPathArrayElementIsObject() {
        final JsonObject json = new JsonParser().parse("{ path: [ { } ] }").getAsJsonObject();
        new ConfigPath(json);
    }

    @Test
    public void testFromJsonPathArrayElementIsNumber() {
        final JsonObject json = new JsonParser().parse("{ path: [ 1, 2 ] }").getAsJsonObject();
        final ConfigPath path = new ConfigPath(json);
        assertEquals(Arrays.asList("1", "2").toString(), path.getPath().toString());
    }

    @Test
    public void testCompareTo() {
        final ConfigPath a = new ConfigPath(TestEnum.TEST_CONFIG_PROPERTY1);
        final ConfigPath b = new ConfigPath(TestEnum.TEST_CONFIG_PROPERTY2);
        final ConfigPath c = new ConfigPath(TestEnum.TEST_CONFIG__PROPERTY2);

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(0, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(0, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ConfigPath a = new ConfigPath(TestEnum.TEST_CONFIG_PROPERTY1);
        final ConfigPath b = new ConfigPath(TestEnum.TEST_CONFIG_PROPERTY2);
        final ConfigPath c = new ConfigPath(TestEnum.TEST_CONFIG__PROPERTY2);

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertEquals(b, c);
        assertNotEquals(c, a);
        assertEquals(c, b);
        assertEquals(c, c);
    }

    @Test
    public void testToString() {
        final ConfigPath path = new ConfigPath(TestEnum.TEST_CONFIG_PROPERTY1);
        assertEquals("ConfigPath[path=[test, config, property1]]", path.toString());
    }

    private enum TestEnum {
        TEST_CONFIG_PROPERTY1,
        TEST_CONFIG_PROPERTY2,
        TEST_CONFIG__PROPERTY2,
    }
}
