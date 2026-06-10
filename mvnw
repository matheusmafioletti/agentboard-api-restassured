#!/bin/sh
# Maven Wrapper script for Unix.
# Downloads Maven 3.9.6 on first run, then delegates all arguments to it.

MAVEN_VERSION="3.9.6"
MAVEN_DIST_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
MAVEN_HOME_DIR="${HOME}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_BIN="${MAVEN_HOME_DIR}/bin/mvn"

if [ ! -f "${MAVEN_BIN}" ]; then
  echo "Downloading Apache Maven ${MAVEN_VERSION}..."
  mkdir -p "${MAVEN_HOME_DIR}"
  TEMP_TGZ=$(mktemp /tmp/apache-maven-XXXXXX.tar.gz)
  curl -fsSL "${MAVEN_DIST_URL}" -o "${TEMP_TGZ}"
  tar -xzf "${TEMP_TGZ}" -C "${MAVEN_HOME_DIR}" --strip-components=1
  rm -f "${TEMP_TGZ}"
fi

exec "${MAVEN_BIN}" "$@"
