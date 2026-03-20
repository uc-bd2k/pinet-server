#!/bin/bash
set -e

cd /app/deepPhosAPI
/opt/deepphos-env/bin/python predict.py &

cd /app
JAVA_HEAP_OPTS="${JAVA_HEAP_OPTS:--Xms512m -Xmx4g}"
JAVA_TOOL_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED" \
  java ${JAVA_HEAP_OPTS} -jar /app/pln.jar --server.port=8091 &

exec python /usr/local/bin/frontend-proxy.py
