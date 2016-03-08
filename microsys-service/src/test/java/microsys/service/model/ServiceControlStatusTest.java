package microsys.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.Test;

/**
 * Perform testing on the {@link ServiceControlStatus} class.
 */
public class ServiceControlStatusTest {
    @Test
    public void testCompareTo() {
        final ServiceControlStatus a = new ServiceControlStatus(true, "stop");
        final ServiceControlStatus b = new ServiceControlStatus(true, "restart");
        final ServiceControlStatus c = new ServiceControlStatus(false, "stop");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(1, a.compareTo(b));
        assertEquals(1, a.compareTo(c));
        assertEquals(-1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(1, b.compareTo(c));
        assertEquals(-1, c.compareTo(a));
        assertEquals(-1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ServiceControlStatus a = new ServiceControlStatus(true, "stop");
        final ServiceControlStatus b = new ServiceControlStatus(true, "restart");
        final ServiceControlStatus c = new ServiceControlStatus(false, "stop");

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
    public void testHashCode() {
        final ServiceControlStatus a = new ServiceControlStatus(true, "stop");
        final ServiceControlStatus b = new ServiceControlStatus(true, "restart");
        final ServiceControlStatus c = new ServiceControlStatus(false, "stop");

        assertEquals(3564267, a.hashCode());
        assertEquals(1097529592, b.hashCode());
        assertEquals(3564304, c.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceControlStatus stat = new ServiceControlStatus(true, "stop");
        assertEquals("ServiceControlStatus[success=true,action=stop]", stat.toString());
    }

    @Test
    public void testToJson() {
        final ServiceControlStatus stat = new ServiceControlStatus(true, "stop");
        assertEquals("{\"success\":true,\"action\":\"stop\"}", stat.toJson().toString());
    }

    @Test
    public void testJsonConstructor() {
        final ServiceControlStatus original = new ServiceControlStatus(true, "stop");
        final ServiceControlStatus copy = new ServiceControlStatus(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoSuccess() {
        new ServiceControlStatus(new JsonParser().parse("{\"action\":\"stop\"}").getAsJsonObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorSuccessWrongType() {
        new ServiceControlStatus(new JsonParser().parse("{\"success\":[],\"action\":\"stop\"}").getAsJsonObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoAction() {
        new ServiceControlStatus(new JsonParser().parse("{\"success\":true}").getAsJsonObject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorActionWrongType() {
        new ServiceControlStatus(new JsonParser().parse("{\"success\":true,\"action\":[]}").getAsJsonObject());
    }

    @Test
    public void testConverter() {
        final ServiceControlStatus.ServiceControlStatusConverter converter =
                new ServiceControlStatus.ServiceControlStatusConverter();
        final ServiceControlStatus original = new ServiceControlStatus(true, "stop");
        final JsonObject json = converter.doBackward(original);
        final ServiceControlStatus copy = converter.doForward(json);
        assertEquals(original, copy);
    }

    @Test
    public void testConverterEquals() {
        final ServiceControlStatus.ServiceControlStatusConverter a =
                new ServiceControlStatus.ServiceControlStatusConverter();
        final ServiceControlStatus.ServiceControlStatusConverter b =
                new ServiceControlStatus.ServiceControlStatusConverter();
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
        final ServiceControlStatus.ServiceControlStatusConverter a =
                new ServiceControlStatus.ServiceControlStatusConverter();
        assertEquals(815372127, a.hashCode());
    }
}
