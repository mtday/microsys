#!/usr/bin/env bash

cd $(dirname $0)

mvn exec:java -pl :microsys-shell -Dexec.mainClass="microsys.shell.runner.Runner"

