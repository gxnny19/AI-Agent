#!/usr/bin/env bash
# POSIX helper to run the Spring Boot app
# Usage: ./run-java.sh
set -euo pipefail

# Build and run via Maven
mvn clean package
mvn spring-boot:run
