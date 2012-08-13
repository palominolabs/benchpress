# Using BenchPress

## Prerequisites

BenchPress requires ZooKeeper, so set up an instance.  The BenchPress controller
will happily coexist with a ZooKeeper instance.

## Building BenchPress

First, build the project from the root directory

    mvn clean install

Create a tarball for both the controller-svc & worker-svc by descending into
the respective directories and running:

    mvn assembly:single

The resulting *-dist.tar.gz files are all you need to deploy BenchPress nodes.

## Runing BenchPress

BenchPress comes in two parts: the controller and one or more workers.  The two
find each other through a common ZooKeeper instance.  You must provide the
connection information for ZooKeeper by setting the ZOOKEEPER_CONNECTION_STRING
environment variable.  Simply set that environment variable and then execute the
run.sh shell script from the above tarball.

## Submitting a job

Jobs are submitted to the controller by PUTing to /job:

    curl -X PUT -H "Content-Type: application/json" -d @test-jobs/hbase.json http://controller:7000/job

The controller will return a simple HTML page listing the active jobs if you GET
/job.  

# Flow of Execution

## Controller

The controller starts in com.palominolabs.benchpress.controller.ControllerMain
which is in the controller-svc package.  It starts up an HTTP server, which is
what a user interacts with, and a JobFarmer, which is responsible for starting
and managing jobs.  When you submit a job to the controller, the JobFarmer uses
the Netflix Curator Service Discovery implementation to find available workers
in ZooKeeper (see JobFarmer:submitJob()), partitions the job, and distributes
it to the workers. The JobFarmer handles status updates from the workers
(handleProgressReport() and handlePartitionFinishedReport() in JobFarmer) and
will provide those reports via getJob().

## Worker
The worker starts in com.palominolabs.benchpress.worker.WorkerMain which is
in the worker-svc package.  It starts up an HTTP server for communicating with
the controller and a WorkerAdvertiser to register itself as available in
ZooKeeper.  A controller wishing to utilize the worker first hits
the /acquireLock/{controllerId} endpoint (see ControlResource) to lock the
worker for its job.  Thereafter, the worker is provided with a Partition for
the job on the /job/{jobId}/partition endpoint.  The worker passes the
Partition to its PartitionRunner, which runs the job, reporting back to the
controller as specified in the job config.

# Futher notes

## Whirr
export AWS_ACCOUNT_ID=
export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=
export EC2_PRIVATE_KEY=
export EC2_CERT=

benchpress $ ssh-keygen -t rsa -P '' -f whirr-benchpress-rsa-key
benchpress $ whirr launch-cluster --config whirr-benchpress-hbase-0.90.properties
benchpress $ grep -m1 -A1 "hbase\.zookeeper\.quorum" whirr.log|grep value|sed -e 's#.*<value>\(.*\)</value>#\1#'

# Administrivia
BenchPress is a project of Palomino Labs.  Find the repository on GitHub
(https://github.com/palominolabs/benchpress) and see the Palomino Labs blog
(http://blog.palominolabs.com) for articles about its development.

