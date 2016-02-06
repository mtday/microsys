#!/bin/sh

keytool -genkey -alias localhost -keyalg RSA -keystore keystore.jks -storepass changeit -dname "CN=localhost" -keypass changeit

cp keystore.jks truststore.jks

keytool -export -file localhost.crt -keystore keystore.jks -storepass changeit -alias localhost


