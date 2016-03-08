package microsys.service.filter;

import com.google.gson.JsonParser;
import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.common.config.CommonConfig;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.service.model.ServiceRequest;
import microsys.service.model.ServiceResponse;
import spark.Filter;
import spark.Request;
import spark.Response;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides a {@link Filter} implementation that processes service request headers, and adds a signed service response
 * header to the response.
 */
public class RequestSigningFilter implements Filter {
    private final static Logger LOG = LoggerFactory.getLogger(RequestSigningFilter.class);

    @Nonnull
    private final CryptoFactory cryptoFactory;
    private final boolean secureMode;

    /**
     * @param config the static system configuration information
     * @param cryptoFactory the {@link CryptoFactory} responsible for signing requests
     */
    public RequestSigningFilter(@Nonnull final Config config, @Nonnull final CryptoFactory cryptoFactory) {
        this.cryptoFactory = Objects.requireNonNull(cryptoFactory);
        this.secureMode = config.getBoolean(CommonConfig.SSL_ENABLED.getKey());
    }

    /**
     * @return the {@link CryptoFactory} responsible for signing requests
     */
    protected CryptoFactory getCryptoFactory() {
        return this.cryptoFactory;
    }

    /**
     * @return whether the system is configured to operate in secure mode
     */
    protected boolean isSecureMode() {
        return this.secureMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@Nonnull final Request request, @Nonnull final Response response) {
        if (!isSecureMode()) {
            // If not in secure mode, no need to do any signing.
            return;
        }

        final Optional<ServiceRequest> serviceRequest = getServiceRequest(request);
        final Optional<ServiceResponse> serviceResponse = getServiceResponse(serviceRequest);
        if (serviceResponse.isPresent()) {
            response.header(ServiceResponse.SERVICE_RESPONSE_HEADER, serviceResponse.get().toJson().toString());
        }
    }

    @Nonnull
    protected Optional<ServiceResponse> getServiceResponse(@Nonnull final Optional<ServiceRequest> serviceRequest) {
        try {
            if (serviceRequest.isPresent()) {
                final String signature = getCryptoFactory().getSymmetricKeyEncryption()
                        .signString(serviceRequest.get().getRequestId(), StandardCharsets.UTF_8);
                return Optional.of(new ServiceResponse(serviceRequest.get().getRequestId(), signature));
            }
        } catch (final EncryptionException encryptionException) {
            LOG.error("Failed to sign service request", encryptionException);
        }
        return Optional.empty();
    }

    @Nonnull
    protected Optional<ServiceRequest> getServiceRequest(@Nonnull final Request request) {
        final Optional<String> header = Optional.ofNullable(request.headers(ServiceRequest.SERVICE_REQUEST_HEADER));
        if (header.isPresent()) {
            return Optional.of(new ServiceRequest(new JsonParser().parse(header.get()).getAsJsonObject()));
        }
        return Optional.empty();
    }
}
