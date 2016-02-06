#!/bin/sh

if [[ -f /etc/bashrc ]]; then
    source /etc/bashrc
fi

# Add ${project.groupId} scripts to the path.
export PATH="${PATH}:/opt/${project.groupId}/current/bin"

