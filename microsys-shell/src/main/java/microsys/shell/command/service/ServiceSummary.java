package microsys.shell.command.service;

/**
 * Used to create a summary line describing the services being displayed in the shell.
 */
class ServiceSummary {
    private final int totalServices;
    private final int matchingServices;

    /**
     * @param totalServices    the total number of services that are up and running
     * @param matchingServices the number of services that match the user-provided shell filter values
     */
    public ServiceSummary(final int totalServices, final int matchingServices) {
        this.totalServices = totalServices;
        this.matchingServices = matchingServices;
    }

    protected int getTotalServices() {
        return this.totalServices;
    }

    protected int getMatchingServices() {
        return this.matchingServices;
    }

    /**
     * @return a summary of the services that are displayed
     */
    @Override
    public String toString() {
        if (getTotalServices() == 0) {
            return "No services are running";
        } else if (getMatchingServices() == 0) {
            return String.format("None of the running services (of which there are %d) match", getTotalServices());
        }
        if (getTotalServices() != getMatchingServices()) {
            if (getMatchingServices() == 1) {
                return String.format("Displaying the matching service (of %d total):", getTotalServices());
            }
            return String.format("Displaying %d matching services (of %d total):",
                    getMatchingServices(), getTotalServices());
        }

        if (getTotalServices() == 1) {
            return "Displaying the single available service:";
        } else if (getTotalServices() == 2) {
            return "Displaying both available services:";
        }
        return String.format("Displaying all %d services:", getTotalServices());
    }
}
