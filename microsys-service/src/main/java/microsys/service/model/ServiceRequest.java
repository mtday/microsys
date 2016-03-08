package microsys.service.model;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.model.Model;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing a service request.
 */
public class ServiceRequest implements Model, Comparable<ServiceRequest> {
    /**
     * The request header that will be used to send {@link ServiceRequest} objects to remote services.
     */
    public final static String SERVICE_REQUEST_HEADER = "SERVICE_REQUEST";

    @Nonnull
    private final String requestId;

    /**
     * The default constructor initializes the request id with a new unique id.
     */
    public ServiceRequest() {
        this.requestId = UUID.randomUUID().toString();
    }

    /**
     * @param requestId the unique identifier of the service request
     */
    public ServiceRequest(@Nonnull final String requestId) {
        this.requestId = Objects.requireNonNull(requestId);
    }

    /**
     * @param json the JSON representation of a {@link ServiceRequest} object
     */
    public ServiceRequest(@Nonnull final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("requestId"), "Request id field required");
        Preconditions.checkArgument(json.get("requestId").isJsonPrimitive(), "Request id field must be a primitive");

        this.requestId = json.get("requestId").getAsString();
    }

    /**
     * @return the unique id associated with the service request
     */
    @Nonnull
    public String getRequestId() {
        return this.requestId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final ServiceRequest other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getRequestId(), other.getRequestId());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof ServiceRequest) && compareTo((ServiceRequest) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getRequestId());
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
        return json;
    }
}
