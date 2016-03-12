package microsys.service.filter;

import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsys.common.config.ConfigKeys;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.service.model.ServiceEnvironment;
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
    private final ServiceEnvironment serviceEnvironment;
    private final boolean secureMode;

    /**
     * @param serviceEnvironment the service environment
     */
    public RequestSigningFilter(@Nonnull final ServiceEnvironment serviceEnvironment) {
        this.serviceEnvironment = Objects.requireNonNull(serviceEnvironment);
        this.secureMode = serviceEnvironment.getConfig().getBoolean(ConfigKeys.SSL_ENABLED.getKey());
    }

    /**
     * @return the {@link CryptoFactory} responsible for signing requests
     */
    protected ServiceEnvironment getServiceEnvironment() {
        return this.serviceEnvironment;
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
                final String signature = getServiceEnvironment().getCryptoFactory().getSymmetricKeyEncryption()
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
