package microsys.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

import microsys.common.model.ServiceType;

/**
 * Perform testing on the {@link Reservation} class.
 */
public class ReservationTest {
    @Test
    public void testCompareTo() {
        final Reservation a = new Reservation(ServiceType.CONFIG, "host", 1234);
        final Reservation b = new Reservation(ServiceType.CONFIG, "host", 1235);
        final Reservation c = new Reservation(ServiceType.CONFIG, "host2", 1235);
        final Reservation d = new Reservation(ServiceType.WEB, "host2", 1235);

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
        final Reservation a = new Reservation(ServiceType.CONFIG, "host", 1234);
        final Reservation b = new Reservation(ServiceType.CONFIG, "host", 1235);
        final Reservation c = new Reservation(ServiceType.CONFIG, "host2", 1235);
        final Reservation d = new Reservation(ServiceType.WEB, "host2", 1235);

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
        final Reservation a = new Reservation(ServiceType.CONFIG, "host", 1234);
        final Reservation b = new Reservation(ServiceType.CONFIG, "host", 1235);
        final Reservation c = new Reservation(ServiceType.CONFIG, "host2", 1235);
        final Reservation d = new Reservation(ServiceType.WEB, "host2", 1235);

        assertEquals(1923115449, a.hashCode());
        assertEquals(1923115450, b.hashCode());
        assertEquals(1189713764, c.hashCode());
        assertEquals(-496343930, d.hashCode());
    }

    @Test
    public void testToString() {
        final Reservation res = new Reservation(ServiceType.CONFIG, "host", 1234);
        assertEquals("Reservation[type=CONFIG,host=host,port=1234]", res.toString());
    }

    @Test
    public void testToJson() {
        final Reservation res = new Reservation(ServiceType.CONFIG, "host", 1234);
        assertEquals("{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":1234}", res.toJson().toString());
    }

    @Test
    public void testJsonConstructor() {
        final Reservation original = new Reservation(ServiceType.CONFIG, "host", 1234);
        final Reservation copy = new Reservation(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoType() {
        final String jsonStr = "{\"host\":\"host\",\"port\":1234}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Reservation(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorTypeWrongType() {
        final String jsonStr = "{\"type\":[],\"host\":\"host\",\"port\":1234}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Reservation(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoHost() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"port\":1234}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Reservation(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorHostWrongType() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":[],\"port\":1234}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Reservation(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoPort() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":\"host\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Reservation(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorPortWrongType() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":[]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Reservation(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorPortNotInt() {
        final String jsonStr = "{\"type\":\"CONFIG\",\"host\":\"host\",\"port\":\"a\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new Reservation(json);
    }
}
