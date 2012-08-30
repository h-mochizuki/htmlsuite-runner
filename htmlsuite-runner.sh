#!/bin/sh
[ ! -f build/libs/htmlsuite-runner-1.0.jar ] && ./gradlew clean assemble
java -jar build/libs/htmlsuite-runner-1.0.jar $1
