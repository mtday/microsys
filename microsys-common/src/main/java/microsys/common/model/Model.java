package microsys.common.model;

import com.google.gson.JsonObject;

/**
 * This interface defines the required functionality for model objects in this system.
 */
public interface Model {
    /**
     * @return a JSON representation of the model object used for serialization
     */
    JsonObject toJson();
}
