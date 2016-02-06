#!/bin/sh

cat <<EOF > /tmp/cdh4.repo
[cloudera-cdh4]
name     = Cloudera's Distribution for Hadoop, Version 4
baseurl  = http://archive.cloudera.com/cdh4/redhat/6/x86_64/cdh/4/
gpgkey   = http://archive.cloudera.com/cdh4/redhat/6/x86_64/cdh/RPM-GPG-KEY-cloudera    
gpgcheck = 1
EOF
sudo mv /tmp/cdh4.repo /etc/yum.repos.d/cdh4.repo

sudo yum install -y java-1.8.0-openjdk-devel
sudo yum install -y vim
sudo yum install -y zookeeper zookeeper-server
sudo yum update -y nss


cat <<EOF > /tmp/java.sh
export JAVA_HOME="/usr/lib/jvm/java-1.8.0-openjdk.x86_64"
export PATH="\${JAVA_HOME}/bin:\${PATH}"
EOF
sudo mv /tmp/java.sh /etc/profile.d/java.sh


echo "Starting zookeeper"
sudo mkdir /var/lib/zookeeper
sudo chown zookeeper:zookeeper /var/lib/zookeeper
sudo -u zookeeper zookeeper-server-initialize
sudo service zookeeper-server start


echo "Installing Maven"
if [[ ! -d /opt/apache-maven ]]; then
    sudo mkdir /opt/apache-maven
    sudo wget -q -O /opt/apache-maven/apache-maven-3.3.9-bin.tar.gz \
            http://mirrors.gigenet.com/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz
    sudo tar xzf /opt/apache-maven/apache-maven-3.3.9-bin.tar.gz -C /opt/apache-maven
    sudo ln -s /opt/apache-maven/apache-maven-3.3.9 /opt/apache-maven/current
fi
cat <<EOF > /tmp/maven.sh
export M2_HOME="/opt/apache-maven/current"
export PATH="\${M2_HOME}/bin:\${PATH}"
EOF
sudo mv /tmp/maven.sh /etc/profile.d/maven.sh



