#!/bin/sh

#
# The RPM post-install script.
#


# Create a symlink for current to the newly installed version.
ln -sf /opt/${project.groupId}/${project.version} /opt/${project.groupId}/current

# Create a symlink for the /etc/init.d service script.
ln -sf /opt/${project.groupId}/current/bin/service.sh /etc/init.d/${project.groupId}

# Symlink the config directory.
ln -sf /etc/sysconfig/${project.groupId} /opt/${project.groupId}/current/config

# Symlink the home directory.
ln -sf /home/${project.groupId} /opt/${project.groupId}/current/home

# Symlink the logs directory.
ln -sf /var/log/${project.groupId} /opt/${project.groupId}/current/logs

# Create the keystore and truststore if they do not exist.
KEYSTORE="/home/${project.groupId}/pki/keystore.jks"
TRUSTSTORE="/home/${project.groupId}/pki/truststore.jks"
if [[ ! -f ${KEYSTORE} ]]; then
    keytool -genkey -alias localhost -keyalg RSA -keystore ${KEYSTORE} -storepass changeit \
        -dname "CN=localhost" -keypass changeit
    chown ${project.groupId}:${project.groupId} ${KEYSTORE}
    chmod 640 ${KEYSTORE}
fi
if [[ ! -f ${TRUSTSTORE} ]]; then
    cp ${KEYSTORE} ${TRUSTSTORE}
    chown ${project.groupId}:${project.groupId} ${TRUSTSTORE}
    chmod 640 ${TRUSTSTORE}
fi

