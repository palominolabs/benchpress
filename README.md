# BenchPress

Check out some blog posts on BenchPress: [an introduction](http://blog.palominolabs.com/2012/06/13/introducing-benchpress-distributed-load-testing-for-nosql-databases/) and [how to make custom task types](http://blog.palominolabs.com/2012/08/17/custom-task-types-with-benchpress/).
## Building BenchPress

First, build the project from the root directory.

    ./gradlew build

Create archives for both `controller-svc` & `worker-svc` by descending into
the respective directories and running:

    ./gradlew distZip

The resulting `.zip` files in the respective `build/distributions` directories are all you need to deploy the core BenchPress code.

## Runing BenchPress

BenchPress comes in two parts: the controller and one or more workers.  The two
find each other through a common ZooKeeper instance. For a simple setup where the
controller and 1 worker live on the same box, you don't need to do anything since
the controller will start up an embedded ZooKeeper server and the worker default
settings assume the controller and ZooKeeper are on localhost.

If you wish to use an external ZooKeeper, you must provide the connection information.

    -Dbenchpress.zookeeper.client.connection-string=zkhost:zkport


In that case, you should inform the controller to not start up an embedded ZooKeeper:

    -Dbenchpress.controller.zookeeper.embedded-server.enable=false


Setting system properties like those is also how you can set any of the other configurable system-level parameters in BenchPress. If you wanted to change the controller's HTTP server IP away from the default 127.0.0.1, you could use

    -Dbenchpress.controller.http-server.ip=1.2.3.4

when starting the controller. Similarly, you can change the worker's http server ip with

    -Dbenchpress.worker.http-server.ip=1.2.3.4

Look in `ControllerConfig` and `WorkerConfig` to see more. (Anything with methods annotated with `@Config` is settable via system properties.)

## Submitting a job

Create a new job by submitting task JSON to the controller via PUTing to `/job`:

    curl -X PUT -H "Content-Type: application/json" -d @path/to/your/file.json http://controller:7000/job

The controller will return a simple HTML page listing the active jobs if you `GET`
`/controller/job`. You'll need to be running the appropriate server (HBase, in this case) and set up the
table and column family.

# Flow of Execution

## Controller

The controller starts in `com.palominolabs.benchpress.controller.ControllerMain`
which is in the `controller-svc` module.  It starts up an HTTP server, which is
what a user interacts with, and a `JobFarmer`, which is responsible for starting
and managing jobs.  When you submit a job to the controller, the `JobFarmer` uses
the Netflix Curator Service Discovery implementation to find available workers
in ZooKeeper (see `JobFarmer.submitJob()`), slices the job, and distributes
it to the workers. The `JobFarmer` handles status updates from the workers and
will provide those reports via `getJob()`.

## Worker
The worker starts in `com.palominolabs.benchpress.worker.WorkerMain` which is
in the `worker-svc` module.  It starts up an HTTP server for communicating with
the controller and a `WorkerAdvertiser` to register itself as available in
ZooKeeper.  A controller wishing to utilize the worker first hits
the `/acquireLock/{controllerId}` endpoint (see `ControlResource`) to lock the
worker for its job.  Thereafter, the worker is provided with a `JobSlice` for
the job on the `/job/{jobId}/slice endpoint`.  The worker passes the
`JobSlice` to its `SliceRunner`, which runs the job, reporting back to the
controller as specified in the job config.

# Custom job types
BenchPress is just a simple way to distribute and execute a workload. You need 
to define what the workload is. To do this, implement `JobTypePlugin`. 
 
There are some example implementations to crib off of. The "sum of squares" example
shows the basic concepts: it performs some fairly trivial work (calculate the sum
of the squares of every number in a range, as in 1^2 + 2^2 + ... + 10^2) by taking
the initial range and slicing it across all available workers. The "multi db" example
is a more complex use case; it applies equivalent workloads to database engines for
rough benchmarking. You can also see `SingleVmIntegrationTest` for a minimal use
of a plugin.

Once you have your custom plugin, you'll need to make it available to the rest of 
BenchPress. To do this, add the jar for your plugin to the 
`benchpress.plugin.module-names` system property. Stay tuned!

# Administrivia
BenchPress is a project of [Palomino Labs](http://palominolabs.com).  Find the repository on GitHub
(https://github.com/palominolabs/benchpress) and see the Palomino Labs blog
(http://blog.palominolabs.com) for articles about its development.
