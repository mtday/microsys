#!/bin/sh

cd "$(dirname $0)"
rm -f *.jks *.crt

keytool -genkey -alias localhost -keyalg RSA -keystore keystore.jks -storepass changeit -dname "CN=localhost" -keypass changeit

cp keystore.jks truststore.jks

keytool -export -file localhost.crt -keystore keystore.jks -storepass changeit -alias localhost
keytool -importcert -file localhost.crt -keystore certificate.jks -storepass changeit -alias localhost -noprompt

keytool -genkey -alias a -keyalg RSA -keystore multiple.jks -storepass changeit -dname "CN=a" -keypass changeit
keytool -genkey -alias b -keyalg RSA -keystore multiple.jks -storepass changeit -dname "CN=b" -keypass changeit

