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
CERTIFICATE="/home/${project.groupId}/pki/localhost.crt"
if [[ ! -f ${KEYSTORE} ]]; then
    # Create a self signed key pair root CA certificate.
    keytool -genkeypair -v \
      -alias localhost \
      -dname "CN=localhost, OU=microsys, O=mtday, C=US" \
      -keystore ${KEYSTORE} \
      -keypass changeit \
      -storepass changeit \
      -keyalg RSA \
      -keysize 4096 \
      -ext KeyUsage="keyCertSign" \
      -ext BasicConstraints:"critical=ca:true" \
      -validity 9999

    chown ${project.groupId}:${project.groupId} ${KEYSTORE}
    chmod 640 ${KEYSTORE}
fi
if [[ ! -f ${TRUSTSTORE} ]]; then
    # Export the public certificate so that it can be used in the trust store.
    keytool -export -v \
      -alias localhost \
      -file ${CERTIFICATE} \
      -keypass changeit \
      -storepass changeit \
      -keystore ${KEYSTORE} \
      -rfc

    # Import the public certificate into a trust store.
    keytool -importcert \
      -file ${CERTIFICATE} \
      -keystore ${TRUSTSTORE} \
      -storepass changeit \
      -alias localhost \
      -noprompt

    chown ${project.groupId}:${project.groupId} ${CERTIFICATE}
    chmod 640 ${CERTIFICATE}

    chown ${project.groupId}:${project.groupId} ${TRUSTSTORE}
    chmod 640 ${TRUSTSTORE}
fi

