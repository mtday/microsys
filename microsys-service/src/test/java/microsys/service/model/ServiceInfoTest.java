package microsys.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import microsys.common.model.service.ServiceType;

/**
 * Perform testing on the {@link ServiceInfo} class.
 */
public class ServiceInfoTest {
    @Test
    public void testCompareTo() {
        final ServiceInfo a = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.3");
        final ServiceInfo b = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.4");
        final ServiceInfo c = new ServiceInfo(ServiceType.CONFIG, "name2", "1.2.3");
        final ServiceInfo d = new ServiceInfo(ServiceType.WEB, "name2", "1.2.3");

        assertEquals(1, a.compareTo(null));

        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(-20, a.compareTo(d));

        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(-20, b.compareTo(d));

        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
        assertEquals(-20, c.compareTo(d));

        assertEquals(20, d.compareTo(a));
        assertEquals(20, d.compareTo(b));
        assertEquals(20, d.compareTo(c));
        assertEquals(0, d.compareTo(d));
    }

    @Test
    public void testEquals() {
        final ServiceInfo a = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.3");
        final ServiceInfo b = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.4");
        final ServiceInfo c = new ServiceInfo(ServiceType.CONFIG, "name2", "1.2.3");
        final ServiceInfo d = new ServiceInfo(ServiceType.WEB, "name2", "1.2.3");

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
        final ServiceInfo a = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.3");
        final ServiceInfo b = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.4");
        final ServiceInfo c = new ServiceInfo(ServiceType.CONFIG, "name2", "1.2.3");
        final ServiceInfo d = new ServiceInfo(ServiceType.WEB, "name2", "1.2.3");

        assertEquals(1975895024, a.hashCode());
        assertEquals(1975895025, b.hashCode());
        assertEquals(1425744348, c.hashCode());
        assertEquals(-260313346, d.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceInfo res = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.3");
        assertEquals("ServiceInfo[type=CONFIG,systemName=name,systemVersion=1.2.3]", res.toString());
    }

    @Test
    public void testToJson() {
        final ServiceInfo res = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.3");
        assertEquals(
                "{\"type\":\"CONFIG\",\"systemName\":\"name\",\"systemVersion\":\"1.2.3\"}", res.toJson().toString());
    }

    @Test
    public void testJsonConstructor() {
        final ServiceInfo original = new ServiceInfo(ServiceType.CONFIG, "name", "1.2.3");
        final ServiceInfo copy = new ServiceInfo(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoType() {
        final String jsonStr = "{\"systemName\":\"systemName\",\"systemVersion\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceInfo(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorTypeWrongType() {
        final String jsonStr = "{\"type\":[],\"systemName\":\"systemName\",\"systemVersion\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceInfo(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoName() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"systemVersion\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceInfo(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNameWrongType() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"systemName\":[],\"systemVersion\":\"1.2.3\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceInfo(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoVersion() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"systemName\":\"systemName\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceInfo(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorVersionWrongType() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"systemName\":\"systemName\",\"systemVersion\":[]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceInfo(json);
    }

    @Test
    public void testConverter() {
        final ServiceInfo.ServiceInfoConverter converter = new ServiceInfo.ServiceInfoConverter();
        final ServiceInfo original = new ServiceInfo(ServiceType.CONFIG, "system-name", "1.2.3");
        final JsonObject json = converter.doBackward(original);
        final ServiceInfo copy = converter.doForward(json);
        assertEquals(original, copy);
    }

    @Test
    public void testConverterEquals() {
        final ServiceInfo.ServiceInfoConverter a = new ServiceInfo.ServiceInfoConverter();
        final ServiceInfo.ServiceInfoConverter b = new ServiceInfo.ServiceInfoConverter();
        final Object c = 5;

        assertNotEquals(a, null);
        assertEquals(a, a);
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(b, a);
        assertEquals(b, b);
        assertNotEquals(b, c);
    }

    @Test
    public void testConverterHashCode() {
        final ServiceInfo.ServiceInfoConverter a = new ServiceInfo.ServiceInfoConverter();
        assertEquals(1800635297, a.hashCode());
    }
}
