#!/bin/bash
set -e

cd /app/deepPhosAPI
DEEPPHOS_UNIPROT_API_BASE="${DEEPPHOS_UNIPROT_API_BASE:-http://127.0.0.1:8090/api/uniprotdb/organism}" \
  /opt/deepphos-env/bin/python predict.py &

cd /app
JAVA_HEAP_OPTS="${JAVA_HEAP_OPTS:--Xms2g -Xmx16g}"
JAVA_TOOL_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED" \
  exec java ${JAVA_HEAP_OPTS} -jar /app/pln.jar --server.port=8090
