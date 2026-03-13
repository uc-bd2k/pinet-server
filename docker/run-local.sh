#!/bin/bash
set -e

cd /app/deepPhosAPI
/opt/deepphos-env/bin/python predict.py &

cd /app
JAVA_TOOL_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED" \
  java -jar /app/pln.jar --server.port=8091 &

exec python /usr/local/bin/frontend-proxy.py
