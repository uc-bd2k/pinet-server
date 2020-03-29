#!/bin/bash

cd /deepPhosAPI
. env/bin/activate
python3 predict.py &

cd /pinet-server
java -classpath libs -jar build/libs/*.jar
