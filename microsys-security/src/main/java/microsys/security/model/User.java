package microsys.security.model;

import com.google.common.base.Converter;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import microsys.common.model.Model;
import microsys.common.util.CollectionComparator;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable class representing an authenticated user in this system.
 */
public class User implements Model, Comparable<User> {
    @Nonnull
    private final String id;
    @Nonnull
    private final String userName;
    @Nonnull
    private final SortedSet<String> roles = new TreeSet<>();

    /**
     * @param id the unique id of this user account
     * @param userName the user name associated with the user account
     * @param roles the roles authorized to the user
     */
    public User(@Nonnull final String id, @Nonnull final String userName, @Nonnull final Collection<String> roles) {
        this.id = Objects.requireNonNull(id);
        this.userName = Objects.requireNonNull(userName);
        this.roles.addAll(Objects.requireNonNull(roles));
    }

    /**
     * @param json the JSON representation of a {@link User} object
     */
    public User(@Nonnull final JsonObject json) {
        Objects.requireNonNull(json);
        Preconditions.checkArgument(json.has("id"), "ID field required");
        Preconditions.checkArgument(json.get("id").isJsonPrimitive(), "ID field must be a primitive");
        Preconditions.checkArgument(json.has("userName"), "UserName field required");
        Preconditions.checkArgument(json.get("userName").isJsonPrimitive(), "UserName field must be a primitive");

        this.id = json.get("id").getAsString();
        this.userName = json.get("userName").getAsString();

        if (json.has("roles")) {
            Preconditions.checkArgument(json.get("roles").isJsonArray(), "Roles field must be an array");
            final JsonArray roleArr = json.get("roles").getAsJsonArray();
            roleArr.forEach(
                    element -> Preconditions.checkArgument(element.isJsonPrimitive(), "Role must be a primitive"));
            roleArr.forEach(element -> this.roles.add(element.getAsString()));
        }
    }

    /**
     * @return the unique id of the user account
     */
    @Nonnull
    public String getId() {
        return this.id;
    }

    /**
     * @return the user name associated with the user account
     */
    @Nonnull
    public String getUserName() {
        return this.userName;
    }

    /**
     * @return the unmodifiable sorted set of roles assigned to this user account
     */
    @Nonnull
    public SortedSet<String> getRoles() {
        return Collections.unmodifiableSortedSet(this.roles);
    }

    /**
     * @param role the role to check for existence in this user
     * @return whether the specified role has been granted to this user
     */
    public boolean hasRole(@Nonnull final String role) {
        return getRoles().contains(Objects.requireNonNull(role));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(@Nullable final User other) {
        if (other == null) {
            return 1;
        }

        final CompareToBuilder cmp = new CompareToBuilder();
        cmp.append(getId(), other.getId());
        cmp.append(getUserName(), other.getUserName());
        cmp.append(getRoles(), other.getRoles(), new CollectionComparator<String>());
        return cmp.toComparison();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(@CheckForNull final Object other) {
        return (other instanceof User) && compareTo((User) other) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final HashCodeBuilder hash = new HashCodeBuilder();
        hash.append(getId());
        hash.append(getUserName());
        hash.append(getRoles());
        return hash.toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String toString() {
        final ToStringBuilder str = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        str.append("id", getId());
        str.append("userName", getUserName());
        str.append("roles", getRoles());
        return str.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public JsonObject toJson() {
        final JsonArray roleArr = new JsonArray();
        getRoles().forEach(roleArr::add);

        final JsonObject json = new JsonObject();
        json.addProperty("id", getId());
        json.addProperty("userName", getUserName());
        json.add("roles", roleArr);
        return json;
    }

    /**
     * Support conversions to and from {@link JsonObject} with this class.
     */
    public static class UserConverter extends Converter<JsonObject, User> {
        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        protected User doForward(@Nonnull final JsonObject jsonObject) {
            return new User(Objects.requireNonNull(jsonObject));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @Nonnull
        protected JsonObject doBackward(@Nonnull final User user) {
            return Objects.requireNonNull(user).toJson();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(@CheckForNull final Object other) {
            return (other instanceof UserConverter);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return getClass().getName().hashCode();
        }
    }
}
