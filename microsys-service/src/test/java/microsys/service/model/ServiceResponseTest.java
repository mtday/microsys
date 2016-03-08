package microsys.service.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;

import microsys.common.config.CommonConfig;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.service.client.ServiceException;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Perform testing on the {@link ServiceResponse} class.
 */
public class ServiceResponseTest {
    @Test
    public void testCompareTo() {
        final ServiceResponse a = new ServiceResponse("id1", "sig1");
        final ServiceResponse b = new ServiceResponse("id1", "sig2");
        final ServiceResponse c = new ServiceResponse("id2", "sig");

        assertEquals(1, a.compareTo(null));
        assertEquals(0, a.compareTo(a));
        assertEquals(-1, a.compareTo(b));
        assertEquals(-1, a.compareTo(c));
        assertEquals(1, b.compareTo(a));
        assertEquals(0, b.compareTo(b));
        assertEquals(-1, b.compareTo(c));
        assertEquals(1, c.compareTo(a));
        assertEquals(1, c.compareTo(b));
        assertEquals(0, c.compareTo(c));
    }

    @Test
    public void testEquals() {
        final ServiceResponse a = new ServiceResponse("id1", "sig1");
        final ServiceResponse b = new ServiceResponse("id1", "sig2");
        final ServiceResponse c = new ServiceResponse("id2", "sig");

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
        final ServiceResponse a = new ServiceResponse("id1", "sig1");
        final ServiceResponse b = new ServiceResponse("id1", "sig2");
        final ServiceResponse c = new ServiceResponse("id2", "sig");

        assertEquals(7403383, a.hashCode());
        assertEquals(7403384, b.hashCode());
        assertEquals(3987181, c.hashCode());
    }

    @Test
    public void testToString() {
        final ServiceResponse res = new ServiceResponse("id", "sig");
        assertEquals("ServiceResponse[requestId=id,signature=sig]", res.toString());
    }

    @Test
    public void testToJson() {
        final ServiceResponse res = new ServiceResponse("id", "sig");
        assertEquals("{\"requestId\":\"id\",\"signature\":\"sig\"}", res.toJson().toString());
    }

    @Test
    public void testJsonConstructor() {
        final ServiceResponse original = new ServiceResponse("id", "sig");
        final ServiceResponse copy = new ServiceResponse(original.toJson());
        assertEquals(original, copy);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoRequestId() {
        final String jsonStr = "{\"signature\":\"sig\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceResponse(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorRequestIdWrongType() {
        final String jsonStr = "{\"requestId\":[],\"signature\":\"sig\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceResponse(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorNoSignature() {
        final String jsonStr = "{\"requestId\":\"id\"}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceResponse(json);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJsonConstructorSignatureWrongType() {
        final String jsonStr = "{\"requestId\":\"id\",\"signature\":[]}";
        final JsonObject json = new JsonParser().parse(jsonStr).getAsJsonObject();
        new ServiceResponse(json);
    }

    @Test(expected = ServiceException.class)
    public void testVerifyNoServiceResponseHeader() throws ServiceException {
        final URL keystore = getClass().getClassLoader().getResource("keystore.jks");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
        map.put(CommonConfig.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.getFile()));
        map.put(CommonConfig.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
        map.put(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final CryptoFactory cryptoFactory = new CryptoFactory(config);

        final ServiceRequest serviceRequest = new ServiceRequest("id");

        final Request request = new Request.Builder().url("http://localhost/").build();
        final Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).build();

        ServiceResponse.verify(config, cryptoFactory, serviceRequest, response);
    }

    @Test(expected = ServiceException.class)
    public void testVerifyWrongId() throws ServiceException {
        final URL keystore = getClass().getClassLoader().getResource("keystore.jks");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
        map.put(CommonConfig.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.getFile()));
        map.put(CommonConfig.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
        map.put(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final CryptoFactory cryptoFactory = new CryptoFactory(config);

        final ServiceRequest serviceRequest = new ServiceRequest("id");

        final Request request = new Request.Builder().url("http://localhost/").build();
        final Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200)
                .addHeader(ServiceResponse.SERVICE_RESPONSE_HEADER, "{\"requestId\":\"WRONG\",\"signature\":\"sig\"}")
                .build();

        ServiceResponse.verify(config, cryptoFactory, serviceRequest, response);
    }

    @Test(expected = ServiceException.class)
    public void testVerifyBadSignature() throws ServiceException {
        final URL keystore = getClass().getClassLoader().getResource("keystore.jks");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
        map.put(CommonConfig.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.getFile()));
        map.put(CommonConfig.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
        map.put(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final CryptoFactory cryptoFactory = new CryptoFactory(config);

        final ServiceRequest serviceRequest = new ServiceRequest("id");

        final Request request = new Request.Builder().url("http://localhost/").build();
        final Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200)
                .addHeader(ServiceResponse.SERVICE_RESPONSE_HEADER, "{\"requestId\":\"id\",\"signature\":\"sign\"}")
                .build();

        ServiceResponse.verify(config, cryptoFactory, serviceRequest, response);
    }

    @Test
    public void testVerifyCorrect() throws ServiceException, EncryptionException {
        final URL keystore = getClass().getClassLoader().getResource("keystore.jks");
        final Map<String, ConfigValue> map = new HashMap<>();
        map.put(CommonConfig.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
        map.put(CommonConfig.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.getFile()));
        map.put(CommonConfig.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
        map.put(CommonConfig.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
        final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
        final CryptoFactory cryptoFactory = new CryptoFactory(config);

        final String signature = cryptoFactory.getSymmetricKeyEncryption().signString("id", StandardCharsets.UTF_8);

        final ServiceRequest serviceRequest = new ServiceRequest("id");

        final Request request = new Request.Builder().url("http://localhost/").build();
        final Response response = new Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200)
                .addHeader(ServiceResponse.SERVICE_RESPONSE_HEADER, "{\"requestId\":\"id\",\"signature\":\"" + signature + "\"}")
                .build();

        ServiceResponse.verify(config, cryptoFactory, serviceRequest, response);
    }
}
