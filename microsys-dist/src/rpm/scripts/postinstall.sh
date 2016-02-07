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

