package microsys.config.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import java.util.Map;
import java.util.SortedSet;

/**
 * Perform testing on the {@link ConfigKeyValueCollection} class.
 */
public class ConfigKeyValueCollectionTest {
    @Test
    public void testConstructor() {
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value");
        final ConfigKeyValueCollection coll = new ConfigKeyValueCollection(kv1, kv2);

        assertEquals(2, coll.size());
        assertTrue(coll.get("key1").isPresent());
        assertTrue(coll.get("key2").isPresent());
    }

    @Test
    public void testFromJson() {
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value");
        final ConfigKeyValueCollection original = new ConfigKeyValueCollection(kv1, kv2);
        final ConfigKeyValueCollection copy = new ConfigKeyValueCollection(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonNoConfig() {
        final JsonObject json = new JsonParser().parse("{ }").getAsJsonObject();
        new ConfigKeyValue(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonConfigNotArray() {
        final JsonObject json = new JsonParser().parse("{ config: 5 }").getAsJsonObject();
        new ConfigKeyValue(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsonConfigArrayWithPrimitives() {
        final JsonObject json = new JsonParser().parse("{ config: [ \"key\" ] }").getAsJsonObject();
        new ConfigKeyValue(json);
    }

    @Test
    public void testAsMap() {
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value");
        final ConfigKeyValueCollection coll = new ConfigKeyValueCollection(kv1, kv2);
        final Map<String, ConfigKeyValue> map = coll.asMap();
        assertEquals(2, map.size());
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertEquals(kv1, map.get("key1"));
        assertEquals(kv2, map.get("key2"));
    }

    @Test
    public void testAsSet() {
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value");
        final ConfigKeyValueCollection coll = new ConfigKeyValueCollection(kv1, kv2);
        final SortedSet<ConfigKeyValue> set = coll.asSet();
        assertEquals(2, set.size());
        assertTrue(set.contains(kv1));
        assertTrue(set.contains(kv2));
    }

    @Test
    public void testCompareTo() {
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value");
        final ConfigKeyValue kv3 = new ConfigKeyValue("key3", "value");
        final ConfigKeyValueCollection a = new ConfigKeyValueCollection(kv1, kv2);
        final ConfigKeyValueCollection b = new ConfigKeyValueCollection(kv2, kv3);
        final ConfigKeyValueCollection c = new ConfigKeyValueCollection(kv3);

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-2, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(2, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value");
        final ConfigKeyValue kv3 = new ConfigKeyValue("key3", "value");
        final ConfigKeyValueCollection a = new ConfigKeyValueCollection(kv1, kv2);
        final ConfigKeyValueCollection b = new ConfigKeyValueCollection(kv2, kv3);
        final ConfigKeyValueCollection c = new ConfigKeyValueCollection(kv3);

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
        final ConfigKeyValue kv1 = new ConfigKeyValue("key1", "value");
        final ConfigKeyValue kv2 = new ConfigKeyValue("key2", "value");
        final ConfigKeyValueCollection coll = new ConfigKeyValueCollection(kv1, kv2);
        assertEquals(
                "ConfigKeyValueCollection[configs=[ConfigKeyValue[key=key1,value=value], ConfigKeyValue[key=key2,"
                        + "value=value]]]",
                coll.toString());
    }
}
