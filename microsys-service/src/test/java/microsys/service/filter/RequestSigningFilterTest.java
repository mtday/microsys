package microsys.service.filter;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import microsys.common.config.ConfigKeys;
import microsys.crypto.CryptoFactory;
import microsys.crypto.impl.DefaultCryptoFactory;
import microsys.service.BaseService;
import microsys.service.model.ServiceRequest;
import microsys.service.model.ServiceResponse;
import spark.Request;
import spark.Response;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Perform testing of the {@link BaseService} class.
 */
public class RequestSigningFilterTest {
    @Test
    public void testRequestSigningFilter() throws Exception {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestSigningFilter.class)).setLevel(Level.OFF);

        final Optional<URL> keystore = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));
        if (keystore.isPresent()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(ConfigKeys.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(ConfigKeys.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.get().getFile()));
            map.put(ConfigKeys.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("JKS"));
            map.put(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
            final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);

            final ServiceRequest serviceRequest = new ServiceRequest("request-id");

            final Request request = Mockito.mock(Request.class);
            Mockito.when(request.headers(ServiceRequest.SERVICE_REQUEST_HEADER))
                    .thenReturn(serviceRequest.toJson().toString());
            final Response response = Mockito.mock(Response.class);

            final RequestSigningFilter filter = new RequestSigningFilter(config, cryptoFactory);

            filter.handle(request, response);

            Mockito.verify(response).header(Mockito.eq(ServiceResponse.SERVICE_RESPONSE_HEADER), Mockito.anyString());
        }
    }

    @Test
    public void testRequestSigningFilterNoServiceRequest() throws Exception {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestSigningFilter.class)).setLevel(Level.OFF);

        final Optional<URL> keystore = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));
        if (keystore.isPresent()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(ConfigKeys.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
            final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);

            final Request request = Mockito.mock(Request.class);
            Mockito.when(request.headers(ServiceRequest.SERVICE_REQUEST_HEADER)).thenReturn(null);
            final Response response = Mockito.mock(Response.class);

            final RequestSigningFilter filter = new RequestSigningFilter(config, cryptoFactory);

            filter.handle(request, response);

            Mockito.verify(response, Mockito.times(0))
                    .header(Mockito.eq(ServiceResponse.SERVICE_RESPONSE_HEADER), Mockito.anyString());
        }
    }

    @Test
    public void testRequestSigningFilterEncryptionFailed() throws Exception {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RequestSigningFilter.class)).setLevel(Level.OFF);

        final Optional<URL> keystore = Optional.ofNullable(getClass().getClassLoader().getResource("keystore.jks"));
        if (keystore.isPresent()) {
            final Map<String, ConfigValue> map = new HashMap<>();
            map.put(ConfigKeys.SSL_ENABLED.getKey(), ConfigValueFactory.fromAnyRef("true"));
            map.put(ConfigKeys.SSL_KEYSTORE_FILE.getKey(), ConfigValueFactory.fromAnyRef(keystore.get().getFile()));
            map.put(ConfigKeys.SSL_KEYSTORE_TYPE.getKey(), ConfigValueFactory.fromAnyRef("WRONG"));
            map.put(ConfigKeys.SSL_KEYSTORE_PASSWORD.getKey(), ConfigValueFactory.fromAnyRef("changeit"));
            final Config config = ConfigFactory.parseMap(map).withFallback(ConfigFactory.load());
            final CryptoFactory cryptoFactory = new DefaultCryptoFactory(config);
            final ServiceRequest serviceRequest = new ServiceRequest("request-id");

            final Request request = Mockito.mock(Request.class);
            Mockito.when(request.headers(ServiceRequest.SERVICE_REQUEST_HEADER))
                    .thenReturn(serviceRequest.toJson().toString());
            final Response response = Mockito.mock(Response.class);

            final RequestSigningFilter filter = new RequestSigningFilter(config, cryptoFactory);

            filter.handle(request, response);

            Mockito.verify(response, Mockito.times(0))
                    .header(Mockito.eq(ServiceResponse.SERVICE_RESPONSE_HEADER), Mockito.anyString());
        }
    }
}
