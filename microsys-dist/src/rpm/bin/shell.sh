#!/bin/sh

SERVICE="${project.groupId}"
SERVICE_USER="${project.user}"

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

if [[ -f "/home/${SERVICE_USER}/.bashrc" ]]; then
    source "/home/${SERVICE_USER}/.bashrc"
fi

# Set the classpath to be used when launching the shell.
_set_classpath() {
    export CLASSPATH="${CONFIG_DIR}/*:${LIB_DIR}/*"
}

# Start the shell process.
_start() {
    # Load configuration parameters if available. The files in the configuration directory
    # can be used to set things like JAVA_OPTS for service-specific memory options.
    if [[ -f "${CONFIG_DIR}/shell" ]]; then
        source "${CONFIG_DIR}/shell"
    fi

    # Set the JAVA_OPTS value if not set.
    if [[ -z ${JAVA_OPTS} ]]; then
        JAVA_OPTS="${JAVA_OPTS} ${DEFAULT_MEMORY_OPTIONS}"
    fi

    _set_classpath

    SHELL_CLASS="microsys.shell.runner.Runner"
    LOG_CONFIG="-Dlogback.configurationFile=${CONFIG_DIR}/logback-shell.xml"
    APP_CONFIG="-Dconfig.file=${CONFIG_DIR}/application.conf"

    java ${JAVA_OPTS} ${LOG_CONFIG} ${APP_CONFIG} ${SHELL_CLASS}
    return $?
}


_start && exit $?

