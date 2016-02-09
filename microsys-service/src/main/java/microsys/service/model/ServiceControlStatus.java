package microsys.service.model;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import microsys.common.model.Model;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

/**
 * An immutable class representing the response from invoking a service control operation.
 */
public class ServiceControlStatus implements Model, Comparable<ServiceControlStatus> {
    private final boolean success;
    private final String action;

    /**
     * @param success whether the control action was successful
     * @param action the control action that was performed
     */
    public ServiceControlStatus(final boolean success, final String action) {
        this.success = success;
        this.action = Objects.requireNonNull(action);
    }

    /**
     * @param json the JSON representation of a {@link ServiceControlStatus} object
     */
    public ServiceControlStatus(final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("success"), "Success field required");
        Preconditions.checkArgument(json.get("success").isJsonPrimitive(), "Success field must be a primitive");
        Preconditions.checkArgument(json.has("action"), "Action field required");
        Preconditions.checkArgument(json.get("action").isJsonPrimitive(), "Action field must be a primitive");

        this.success = json.get("success").getAsBoolean();
        this.action = json.get("action").getAsString();
    }

    /**
     * @return whether the control action was successful
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * @return the control action that was performed
     */
    public String getAction() {
        return this.action;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final ServiceControlStatus other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(isSuccess(), other.isSuccess());
        cmp.append(getAction(), other.getAction());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        return (other instanceof ServiceControlStatus) && compareTo((ServiceControlStatus) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(isSuccess());
        hash.append(getAction());
        return hash.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("success", isSuccess());
        str.append("action", getAction());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("success", isSuccess());
        json.addProperty("action", getAction());
        return json;
    }

    /**
     * Support conversions to and from {@link JsonObject} with this class.
     */
    public static class ServiceControlStatusConverter extends Converter<JsonObject, ServiceControlStatus> {
        /**
         * {@inheritDoc}
         */
        @Override
        protected ServiceControlStatus doForward(final JsonObject jsonObject) {
            return new ServiceControlStatus(Objects.requireNonNull(jsonObject));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JsonObject doBackward(final ServiceControlStatus serviceInfo) {
            return Objects.requireNonNull(serviceInfo).toJson();
        }
    }
}
