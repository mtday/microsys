#!/bin/sh

SERVICE="${project.groupId}"
SERVICE_USER="${project.user}"
SERVICE_NAME="$1"

CONFIG_DIR="/etc/sysconfig/${SERVICE}"
LIB_DIR="/opt/${SERVICE}/current/lib"
LOG_DIR="/var/log/${SERVICE}"
VAR_DIR="/var/run/${SERVICE}"

DEFAULT_MEMORY_OPTIONS="-Xmx200m -Xms200m"

# Only usable for the service user.
if [ "$(whoami)" != "${SERVICE_USER}" ]; then
    echo "${SERVICE}: Only usable by ${SERVICE_USER}"
    exit 4
fi

# Determine the service class given a service name.
_set_service_class() {
    SERVICE_NAME="$1"
    SERVICE_CLASS="${SERVICE}.${SERVICE_NAME}.runner.Runner"
}

# Set the classpath to be used when launching the service.
_set_classpath() {
    SERVICE_NAME="$1"
    export CLASSPATH="${CONFIG_DIR}/*:${LIB_DIR}/*"
}

# Start the specified service.
_start() {
    # Load configuration parameters if available. The files in the configuration directory
    # can be used to set things like JAVA_OPTS for service-specific memory options.
    if [[ -f "${CONFIG_DIR}/${SERVICE_NAME}" ]]; then
        source "${CONFIG_DIR}/${SERVICE_NAME}"
    fi

    # Set the JAVA_OPTS value if not set.
    if [[ -z ${JAVA_OPTS} ]]; then
        JAVA_OPTS="${JAVA_OPTS} ${DEFAULT_MEMORY_OPTIONS}"
    fi

    _set_service_class ${SERVICE_NAME}
    _set_classpath ${SERVICE_NAME}

    LOG_CONFIG="-Dlogback.configurationFile=${CONFIG_DIR}/logback-${SERVICE_NAME}.xml"
    STDOUT="${LOG_DIR}/${SERVICE_NAME}-stdout.log"

    #java ${JAVA_OPTS} ${LOG_CONFIG} -cp "${CLASSPATH}" ${SERVICE_CLASS} 2>&1 >${STDOUT} &
    java ${JAVA_OPTS} ${LOG_CONFIG} ${SERVICE_CLASS} 2>&1 >${STDOUT} &

    PID=$!
    STARTED=$?

    if [[ ${STARTED} -eq 0 ]]; then
        PID_FILE="${VAR_DIR}/${SERVICE}-${SERVICE_NAME}.pid"
        echo ${PID} > ${PID_FILE}
        return 0
    else
        return 1
    fi
}


_start && exit $?

