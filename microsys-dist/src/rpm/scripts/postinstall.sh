#!/bin/sh

#
# The RPM post-install script.
#


# Create a symlink for current to the newly installed version.
ln -sf /opt/${project.groupId}/${project.version} /opt/${project.groupId}/current

# Create a symlink for the /etc/init.d service script.
ln -sf /opt/${project.groupId}/current/bin/service.sh /etc/init.d/${project.groupId}


# Symlink the logs directory.
if [[ ! -d /var/log/${project.groupId} ]]; then
    mkdir /var/log/${project.groupId}
    chown ${project.user}:${project.group} /var/log/${project.groupId}
fi
ln -sf /var/log/${project.groupId} /opt/${project.groupId}/current/logs

