#!/bin/sh
[ ! -f build/libs/htmlsuite-runner-1.0.jar ] && ./gradlew clean assemble
java -Dfile.encoding=UTF-8 -Dgroovy.file.encoding=UTF-8 -jar build/libs/htmlsuite-runner-1.0.jar $1
