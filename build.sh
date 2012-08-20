#!/bin/bash
set -e

rm -f controller-svc-dist.tar.gz worker-svc-dist.tar.gz

mvn clean install

cd controller-svc/
mvn package assembly:single -DskipTests
cp target/controller-svc-dist.tar.gz ../
cd ..

cd worker-svc/
mvn package assembly:single -DskipTests
cp target/worker-svc-dist.tar.gz ../
cd ..
