package microsys.service.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.typesafe.config.Config;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.config.CommonConfig;
import microsys.common.model.Model;
import microsys.crypto.CryptoFactory;
import microsys.crypto.EncryptionException;
import microsys.service.client.ServiceException;
import okhttp3.Response;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing a service response.
 */
public class ServiceResponse implements Model, Comparable<ServiceResponse> {
    /**
     * The request header that will be used to send {@link ServiceResponse} objects back to the caller.
     */
    public final static String SERVICE_RESPONSE_HEADER = "SERVICE_RESPONSE";

    @Nonnull
    private final String requestId;
    @Nonnull
    private final String signature;

    /**
     * @param requestId the unique identifier of the service request to which this response corresponds
     * @param signature the signature associated with the request id
     */
    public ServiceResponse(@Nonnull final String requestId, @Nonnull final String signature) {
        this.requestId = Objects.requireNonNull(requestId);
        this.signature = Objects.requireNonNull(signature);
    }

    /**
     * @param json the JSON representation of a {@link ServiceResponse} object
     */
    public ServiceResponse(@Nonnull final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("requestId"), "Request id field required");
        Preconditions.checkArgument(json.get("requestId").isJsonPrimitive(), "Request id field must be a primitive");
        Preconditions.checkArgument(json.has("signature"), "Signature field required");
        Preconditions.checkArgument(json.get("signature").isJsonPrimitive(), "Signature field must be a primitive");

        this.requestId = json.get("requestId").getAsString();
        this.signature = json.get("signature").getAsString();
    }

    /**
     * Verify the provided {@link ServiceRequest} matches the response.
     * @param config the static system configuration information
     * @param cryptoFactory the {@link CryptoFactory} used to verify the response signature
     * @param serviceRequest the request to verify
     * @param response the {@link Response} to verify
     * @throws ServiceException if there is a problem verifying the response
     */
    public static void verify(
            @Nonnull final Config config, @Nonnull final CryptoFactory cryptoFactory,
            @Nonnull final ServiceRequest serviceRequest, @Nonnull final Response response) throws ServiceException {
        if (!config.getBoolean(CommonConfig.SSL_ENABLED.getKey())) {
            // No need to verify since security is disabled.
            return;
        }

        final Optional<String> header =
                Optional.ofNullable(Objects.requireNonNull(response).header(SERVICE_RESPONSE_HEADER));
        if (!header.isPresent()) {
            throw new ServiceException("No service response header provided.");
        }

        final ServiceResponse serviceResponse =
                new ServiceResponse(new JsonParser().parse(header.get()).getAsJsonObject());

        if (!Objects.requireNonNull(serviceRequest).getRequestId().equals(serviceResponse.getRequestId())) {
            throw new ServiceException("The id specified in the response does not match the id from the request.");
        }

        try {
            Objects.requireNonNull(cryptoFactory).getSymmetricKeyEncryption()
                    .verifyString(serviceResponse.getRequestId(), StandardCharsets.UTF_8,
                            serviceResponse.getSignature());
        } catch (final EncryptionException encryptionException) {
            throw new ServiceException("Failed to verify response signature", encryptionException);
        }
    }

    /**
     * @return the unique id associated with the service request to which this response corresponds
     */
    @Nonnull
    public String getRequestId() {
        return this.requestId;
    }

    /**
     * @return the signature associated with the request id
     */
    @Nonnull
    public String getSignature() {
        return this.signature;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final ServiceResponse other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getRequestId(), other.getRequestId());
        cmp.append(getSignature(), other.getSignature());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof ServiceResponse) && compareTo((ServiceResponse) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getRequestId());
        hash.append(getSignature());
        return hash.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("requestId", getRequestId());
        str.append("signature", getSignature());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("requestId", getRequestId());
        json.addProperty("signature", getSignature());
        return json;
    }
}
