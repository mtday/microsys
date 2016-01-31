package microsys.shell.model;

/**
 * Represents the status result of a command execution.
 */
public enum CommandStatus {
    /**
     * Indicates that the command completed successfully.
     */
    SUCCESS,

    /**
     * Indicates that the command resulted in a user error.
     */
    FAILED,

    /**
     * Indicates that the command has decided that the shell should terminate.
     */
    TERMINATE
}
