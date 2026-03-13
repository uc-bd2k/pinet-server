#!/bin/bash
set -e

cd /app/deepPhosAPI
python predict.py &

cd /app
java -jar /app/pln.jar --server.port=8091 &

exec python /usr/local/bin/frontend-proxy.py
