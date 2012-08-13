#!/bin/sh

MAIN_CLASS=com.palominolabs.benchpress.worker.WorkerMain

INTERNAL_IP_CONFIG_KEY="benchpress.worker.http-server.ip"
ZOOKEEPER_CONNECTION_STRING_KEY="benchpress.zookeeper.client.connection-string"
#ZOOKEEPER_CONNECTION_STRING=""

source `dirname $0`/run-inner.sh
