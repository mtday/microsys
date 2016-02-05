package microsys.config.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

/**
 * Perform testing on the {@link ConfigKeyValue} class.
 */
public class ConfigKeyValueTest {
    @Test
    public void testConstructor() {
        final ConfigKeyValue kv = new ConfigKeyValue("key", "value");
        assertEquals("key", kv.getKey());
        assertEquals("value", kv.getValue());
    }

    @Test
    public void testFromJson() {
        final ConfigKeyValue original = new ConfigKeyValue("key", "value");
        final ConfigKeyValue copy = new ConfigKeyValue(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonNoKey() {
        final JsonObject json = new JsonParser().parse("{ }").getAsJsonObject();
        new ConfigKeyValue(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonKeyNotPrimitive() {
        final JsonObject json = new JsonParser().parse("{ key: [ ] }").getAsJsonObject();
        new ConfigKeyValue(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonNoValue() {
        final JsonObject json = new JsonParser().parse("{ key: \"key\" }").getAsJsonObject();
        new ConfigKeyValue(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonValueNotPrimitive() {
        final JsonObject json = new JsonParser().parse("{ key: \"key\", value: [ ] }").getAsJsonObject();
        new ConfigKeyValue(json);
    }

    @Test
    public void testCompareTo() {
        final ConfigKeyValue a = new ConfigKeyValue("key", "value");
        final ConfigKeyValue b = new ConfigKeyValue("key", "value2");
        final ConfigKeyValue c = new ConfigKeyValue("another.key", "value");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(10, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(10, b.compareTo(c));
        assertEquals(-10, c.compareTo(a));
        assertEquals(-10, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ConfigKeyValue a = new ConfigKeyValue("key", "value");
        final ConfigKeyValue b = new ConfigKeyValue("key", "value2");
        final ConfigKeyValue c = new ConfigKeyValue("another.key", "value");

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
        assertNotEquals(c, a);
        assertNotEquals(c, b);
        assertEquals(c, c);
    }

    @Test
    public void testToString() {
        final ConfigKeyValue path = new ConfigKeyValue("key", "value");
        assertEquals("ConfigKeyValue[key=key,value=value]", path.toString());
    }
}
