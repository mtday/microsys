#!/bin/sh
#
# microsys	Start microsys system services
#
# chkconfig: 2345 40 40
# description:	Perform management operations on microsys system services
#
# config: /etc/sysconfig/microsys
#
### BEGIN INIT INFO
# Provides: microsys
# Required-Start:
# Required-Stop:
# Default-Start: 2 3 4 5
# Default-Stop: 0 1 6
# Short-Description: microsys system services
# Description: Perform management operations on microsys system services
### END INIT INFO

# Source function library.
. /etc/init.d/functions

SERVICE="${project.groupId}"
SERVICE_USER="${project.user}"

CONFIG_DIR="/etc/sysconfig/${SERVICE}"
VAR_DIR="/var/run/${SERVICE}"


# Only usable for root.
if [ ${EUID} -ne 0 ]; then
    echo -n $"${SERVICE}: Only usable by root."; failure; echo
    exit 4
fi

# Retrieve the list of services available on this node.
_set_services() {
    if [[ -f "${CONFIG_DIR}/services" ]]; then
        SERVICES="$(awk '/^[a-z]+$/' ${CONFIG_DIR}/services)"
        return 0
    else
        echo -n "${SERVICE}: missing config/services file"; failure; echo
        return 1
    fi
}

# Check to see if the specified service is running.
_check_running() {
    SERVICE_NAME="$1"
    PID_FILE="${VAR_DIR}/${SERVICE}-${SERVICE_NAME}.pid"

    # First check the pid file
    if [[ -f "${PID_FILE}" ]]; then
        # The file exists so presumably the service is running.
        PID="$(cat ${PID_FILE})"
        if [[ -z ${PID} ]]; then
            # The pid file exists but was empty for some reason.
            # Delete the pid file and return 1 to indicate not running.
            rm -f ${PID_FILE}
            PID=0 # not running
        else
            # Check to verify that the service pid is running.
            ps -p ${PID} >/dev/null
            IS_RUNNING=$? # 0 if running, 1 if not
            if [[ ${IS_RUNNING} -ne 0 ]]; then
                # The pid file exists, but the pid does not seem to be running.
                # Delete the pid file and return 1 to indicate not running.
                rm -f ${PID_FILE}
                PID=0 # not running
            fi
        fi
    else
        # The file does not exist so presumably the service is not running.
        PID=0 # not running
    fi
}

# Start the specified service.
_start_service() {
    SERVICE_NAME="$1"

    # Check to see if the service is already running. Sets PID variable.
    _check_running ${SERVICE_NAME}

    if [[ ${PID} -ne 0 ]]; then
        echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) already running with pid ${PID}"; success; echo
        return 0
    else
        RUN_SCRIPT="/opt/${SERVICE}/current/bin/run-service.sh"
        runuser -s /bin/bash ${SERVICE_USER} -c "${RUN_SCRIPT} ${SERVICE_NAME}"
        STARTED=$?

        if [[ ${STARTED} -eq 0 ]]; then
            # Verify that the service is running. Sets PID variable.
            _check_running ${SERVICE_NAME}

            if [[ ${PID} -ne 0 ]]; then
                echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) started with pid ${PID}"; success; echo
                return 0
            else
                echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) failed to start"; success; echo
                return 0
            fi
        else
            echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) failed to start"; failure; echo
            return 1
        fi
    fi
}

# Start all the system services for this node.
_start() {
    _set_services || return $?
    RETVAL=0
    for SERVICE_NAME in ${SERVICES}; do
        _start_service ${SERVICE_NAME}
        let RETVAL+=$?
    done
    return ${RETVAL}
}

# Stop the specified service.
_stop_service() {
    SERVICE_NAME="$1"

    # Check to see if the service is running. Sets PID variable.
    _check_running ${SERVICE_NAME}

    if [[ ${PID} -eq 0 ]]; then
        echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) is not running"; success; echo
        return 0
    else
        # Kill gracefully with SIGTERM, which will usually be sufficient.
        kill -1 ${PID}

        # No longer running so remove the pid file.
        PID_FILE="${VAR_DIR}/${SERVICE}-${SERVICE_NAME}.pid"
        rm -f ${PID_FILE}

        echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) stopped"; success; echo
        return 0
    fi
}

# Stop all the system services for this node.
_stop() {
    _set_services || return $?
    RETVAL=0
    for SERVICE_NAME in ${SERVICES}; do
        _stop_service ${SERVICE_NAME}
        let RETVAL+=$?
    done
    return ${RETVAL}
}

# Check the status of the specified service.
_status_service() {
    SERVICE_NAME="$1"

    # Check to see if the service is running. Sets PID variable.
    _check_running ${SERVICE_NAME}

    if [[ ${PID} -ne 0 ]]; then
        echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) running on pid ${PID}"; success; echo
        return 0
    else
        echo -n "${SERVICE}-$(printf "%-8s" ${SERVICE_NAME}) is not running"; warning; echo
        return 1
    fi
}

# Check the status of all the system services for this node.
_status() {
    _set_services || return $?
    RETVAL=0
    for SERVICE_NAME in ${SERVICES}; do
        _status_service ${SERVICE_NAME}
        let RETVAL+=$?
    done
    return ${RETVAL}
}

# Restart all of the system services for this node.
_restart() {
    _stop
    _start
}

# Launch the shell.
_shell() {
    RUN_SCRIPT="/opt/${SERVICE}/current/bin/shell.sh"
    runuser -s /bin/bash ${SERVICE_USER} -c "${RUN_SCRIPT}"
}

case "$1" in
    start)
        _start
        RETVAL=$?
        ;;

    stop)
        _stop
        RETVAL=$?
        ;;

    restart)
        _restart
        RETVAL=$?
        ;;

    status)
        _status
        RETVAL=$?
        ;;

    shell)
        _shell
        RETVAL=$?
        ;;

    *)
        echo $"Usage: ${SERVICE} {start|stop|restart|status|shell}"
        RETVAL=2
        ;;
esac

exit ${RETVAL}

