package microsys.common.model.service;

/**
 * Defines the available service types within this system.
 */
public enum ServiceType {
    /**
     * The dynamic configuration service.
     */
    CONFIG,

    /**
     * The system service that manages service and infrastructure health.
     */
    HEALTH,

    /**
     * The system security service.
     */
    SECURITY,

    /**
     * The web user interface service.
     */
    WEB,
}
