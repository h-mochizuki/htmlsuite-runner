#!/bin/sh
./gradlew clean assemble
java -jar build/libs/htmlsuite-runner-1.0.jar $1
