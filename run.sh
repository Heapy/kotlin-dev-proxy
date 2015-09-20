#!/bin/bash

./gradlew clean installDist

./build/install/kotlin-dev-proxy/bin/kotlin-dev-proxy
