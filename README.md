# Using BenchPress

## Building BenchPress

First, build the project from the root directory.

    mvn clean install

Create a tarball for both `controller-svc` & `worker-svc` by descending into
the respective directories and running:

    mvn package assembly:single -DskipTests

The resulting *-dist.tar.gz files in the respective `target` directories are all you need to deploy BenchPress nodes.

## Runing BenchPress

BenchPress comes in two parts: the controller and one or more workers.  The two
find each other through a common ZooKeeper instance. For a simple setup where the
controller and 1 worker live on the same box, you don't need to do anything since
the controller will start up an embedded ZooKeeper server and the worker default
settings assume the controller and ZooKeeper are on localhost.

If you wish to use an external ZooKeeper, you must provide the connection information.

    sh run.sh -Dbenchpress.zookeeper.client.connection-string=zkhost:zkport


In that case, you should inform the controller to not start up an embedded ZooKeeper:

    -Dbenchpress.controller.zookeeper.embedded-server.enable=false


Setting system properties like those is also how you can set any of the other configurable system-level parameters in BenchPress. If you wanted to change the controller's HTTP server IP away from the default 127.0.0.1, you could use

    -Dbenchpress.controller.http-server.ip=1.2.3.4

when starting the controller. Similarly, you can change the worker's http server ip with

    -Dbenchpress.worker.http-server.ip=1.2.3.4

Look in `ControllerConfig` and `WorkerConfig` to see more. (Anything with methods annotated with `@Config` is settable via system properties.)

## Submitting a job

Jobs are submitted to the controller by PUTing to `/job`:

    curl -X PUT -H "Content-Type: application/json" -d @test-jobs/hbase.json http://controller:7000/job

The controller will return a simple HTML page listing the active jobs if you `GET`
`/job`. You'll need to be running the appropriate server (HBase, in this case) and set up the
table and column family.

# Flow of Execution

## Controller

The controller starts in `com.palominolabs.benchpress.controller.ControllerMain`
which is in the `controller-svc` module.  It starts up an HTTP server, which is
what a user interacts with, and a `JobFarmer`, which is responsible for starting
and managing jobs.  When you submit a job to the controller, the `JobFarmer` uses
the Netflix Curator Service Discovery implementation to find available workers
in ZooKeeper (see `JobFarmer.submitJob()`), partitions the job, and distributes
it to the workers. The `JobFarmer` handles status updates from the workers
(`handleProgressReport()` and `handlePartitionFinishedReport()` in `JobFarmer`) and
will provide those reports via `getJob()`.

## Worker
The worker starts in `com.palominolabs.benchpress.worker.WorkerMain` which is
in the `worker-svc` module.  It starts up an HTTP server for communicating with
the controller and a `WorkerAdvertiser` to register itself as available in
ZooKeeper.  A controller wishing to utilize the worker first hits
the `/acquireLock/{controllerId}` endpoint (see `ControlResource`) to lock the
worker for its job.  Thereafter, the worker is provided with a `Partition` for
the job on the `/job/{jobId}/partition endpoint`.  The worker passes the
`Partition` to its `PartitionRunner`, which runs the job, reporting back to the
controller as specified in the job config.

# Futher notes

## Custom job types
If you want to use a storage system that's not supported out of the box, or you want more flexibility than the current simple JSON structure allows, you can register your own job types. More documentation is coming soon (pending the completion of an in-progress
refactoring of job handling), but if you're impatient you can use `HbaseModule` as a starting point. After setting up a binding to
the appropriate Guice multibinding and using the `@Id` annotation appropriately, you can then add your custom module name(s)
in a comma-separated list as the value of the `benchpress.worker.plugin.module-names` system property. Stay tuned!

## Whirr
Some spartan notes on getting started with [Whirr](http://whirr.apache.org/).
```
export AWS_ACCOUNT_ID=
export AWS_ACCESS_KEY_ID=
export AWS_SECRET_ACCESS_KEY=
export EC2_PRIVATE_KEY=
export EC2_CERT=

benchpress $ ssh-keygen -t rsa -P '' -f whirr-benchpress-rsa-key
benchpress $ whirr launch-cluster --config whirr-benchpress-hbase-0.90.properties
benchpress $ grep -m1 -A1 "hbase\.zookeeper\.quorum" whirr.log|grep value|sed -e 's#.*<value>\(.*\)</value>#\1#'
```

# Administrivia
BenchPress is a project of [Palomino Labs](http://palominolabs.com).  Find the repository on GitHub
(https://github.com/palominolabs/benchpress) and see the Palomino Labs blog
(http://blog.palominolabs.com) for articles about its development.
