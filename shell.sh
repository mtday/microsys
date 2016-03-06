#!/usr/bin/env bash

cd $(dirname $0)

export SHARED_SECRET="abcdABCD1234!@#$"

mvn exec:java -pl :microsys-shell -Dexec.mainClass="microsys.shell.runner.Runner"

