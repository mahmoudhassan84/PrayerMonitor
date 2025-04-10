#!/bin/bash
# Gradle wrapper script

# Gradle version
GRADLE_VERSION=7.6.1

# Gradle distribution URL
GRADLE_DIST_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"

# Gradle home directory
GRADLE_USER_HOME="${HOME}/.gradle"

# Download Gradle if not already downloaded
if [ ! -d "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
    wget -q -O "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin/gradle-${GRADLE_VERSION}-bin.zip" "${GRADLE_DIST_URL}"
    unzip -q "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin/gradle-${GRADLE_VERSION}-bin.zip" -d "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin"
    mv "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin/gradle-${GRADLE_VERSION}"/* "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin/"
    rmdir "${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin/gradle-${GRADLE_VERSION}"
fi

# Run Gradle
"${GRADLE_USER_HOME}/wrapper/dists/gradle-${GRADLE_VERSION}-bin/bin/gradle" "$@"
