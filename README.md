[![Gitter chat](http://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-brightgreen.svg)](https://gitter.im/openzipkin/zipkin)
[![Build Status](https://travis-ci.org/openzipkin/zipkin.svg?branch=master)](https://travis-ci.org/openzipkin/zipkin) [![Download](https://api.bintray.com/packages/openzipkin/maven/zipkin/images/download.svg) ](https://bintray.com/openzipkin/maven/zipkin/_latestVersion)
[![Maven Central](https://img.shields.io/maven-central/v/io.zipkin/zipkin-server.svg)](https://search.maven.org/search?q=g:io.zipkin%20AND%20a:zipkin-server)

# zipkin
[Zipkin](http://zipkin.io) is a distributed tracing system. It helps gather timing data needed to troubleshoot latency problems in microservice architectures. It manages both the collection and lookup of this data. Zipkin’s design is based on the [Google Dapper paper](http://research.google.com/pubs/pub36356.html).

This project includes a dependency-free library and a [spring-boot](http://projects.spring.io/spring-boot/) server. Storage options include in-memory, JDBC (mysql), Cassandra, and Elasticsearch.

## Quick-start

The quickest way to get started is to fetch the [latest released server](https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=LATEST&c=exec) as a self-contained executable jar. Note that the Zipkin server requires minimum JRE 8. For example:

```bash
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

You can also start Zipkin via Docker.
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

Once the server is running, you can view traces with the Zipkin UI at `http://your_host:9411/zipkin/`.

If your applications aren't sending traces, yet, configure them with [Zipkin instrumentation](https://zipkin.io/pages/tracers_instrumentation) or try one of our [examples](https://github.com/openzipkin?utf8=%E2%9C%93&q=example).

Check out the [`zipkin-server`](/zipkin-server) documentation for configuration details, or [`docker-zipkin`](https://github.com/openzipkin/docker-zipkin) for how to use docker-compose.

## Core Library
The [core library](zipkin/src/main/java/zipkin2) is used by both Zipkin instrumentation and the Zipkin server. Its minimum Java language level is 6, in efforts to support those writing agent instrumentation.

This includes built-in codec for Zipkin's v1 and v2 json formats. A direct dependency on gson (json library) is avoided by minifying and repackaging classes used. The result is a 155k jar which won't conflict with any library you use.

Ex.
```java
// All data are recorded against the same endpoint, associated with your service graph
localEndpoint = Endpoint.newBuilder().serviceName("tweetie").ip("192.168.0.1").build()
span = Span.newBuilder()
    .traceId("d3d200866a77cc59")
    .id("d3d200866a77cc59")
    .name("targz")
    .localEndpoint(localEndpoint)
    .timestamp(epochMicros())
    .duration(durationInMicros)
    .putTag("compression.level", "9");

// Now, you can encode it as json
bytes = SpanBytesEncoder.JSON_V2.encode(span);
```

Note: The above is just an example, most likely you'll want to use an existing tracing library like [Brave](https://github.com/openzipkin/brave)

## Storage Component
Zipkin includes a [StorageComponent](zipkin/src/main/java/zipkin2/storage/StorageComponent.java), used to store and query spans and
dependency links. This is used by the server and those making custom
servers, collectors, or span reporters. For this reason, storage
components have minimal dependencies, but most require Java 8+

Ex.
```java
// this won't create network connections
storage = ElasticsearchStorage.newBuilder()
                              .hosts(asList("http://myelastic:9200")).build();

// prepare a call
traceCall = storage.spanStore().getTrace("d3d200866a77cc59");

// execute it synchronously or asynchronously
trace = traceCall.execute();

// clean up any sessions, etc
storage.close();
```

### In-Memory
The [InMemoryStorage](zipkin/src/main/java/zipkin2/storage/InMemoryStorage.java) component is packaged in zipkin's core library. It
is neither persistent, nor viable for realistic work loads. Its purpose
is for testing, for example starting a server on your laptop without any
database needed.

### Cassandra
The [Cassandra](zipkin-storage/cassandra) component uses Cassandra
3.11.3+ features, but is tested against the latest patch of Cassandra 3.11.

This is the second generation of our Cassandra schema. It stores spans
using UDTs, such that they appear like Zipkin v2 json in cqlsh. It is
designed for scale, and uses a combination of SASI and manually
implemented indexes to make querying larger data more performant.

Note: This store requires a [job to aggregate](https://github.com/openzipkin/zipkin-dependencies) dependency links.

### Elasticsearch
The [Elasticsearch](zipkin-storage/elasticsearch) component uses
Elasticsearch 5+ features, but is tested against Elasticsearch 6-7.x.

It stores spans as Zipkin v2 json so that integration with other tools is
straightforward. To help with scale, this uses a combination of custom
and manually implemented indexing.

Note: This store requires a [spark job](https://github.com/openzipkin/zipkin-dependencies) to aggregate dependency links.

### Disabling search
The following API endpoints provide search features, and are enabled by
default. Search primarily allows the trace list screen of the UI operate.
* `GET /services` - Distinct Span.localServiceName
* `GET /remoteServices?serviceName=X` - Distinct Span.remoteServiceName by Span.localServiceName
* `GET /spans?serviceName=X` - Distinct Span.name by Span.localServiceName
* `GET /autocompleteKeys` - Distinct keys of Span.tags subject to configurable whitelist
* `GET /autocompleteValues?key=X` - Distinct values of Span.tags by key
* `GET /traces` - Traces matching a query possibly including the above criteria


When search is disabled, traces can only be retrieved by ID
(`GET /trace/{traceId}`). Disabling search is only viable when there is
an alternative way to find trace IDs, such as logs. Disabling search can
reduce storage costs or increase write throughput.

`StorageComponent.Builder.searchEnabled(false)` is implied when a zipkin
is run with the env variable `SEARCH_ENABLED=false`.

### Legacy (v1) components
The following components are no longer encouraged, but exist to help aid
transition to supported ones. These are indicated as "v1" as they use
data layouts based on Zipkin's V1 Thrift model, as opposed to the
simpler v2 data model currently used.

#### MySQL
The [MySQL v1](zipkin-storage/mysql-v1) component uses MySQL 5.6+
features, but is tested against MariaDB 10.3.

The schema was designed to be easy to understand and get started with;
it was not designed for performance. Ex spans fields are columns, so
you can perform ad-hoc queries using SQL. However, this component has
[known performance issues](https://github.com/openzipkin/zipkin/issues/1233): queries will eventually take seconds to return
if you put a lot of data into it.

This store does not require a [job to aggregate](https://github.com/openzipkin/zipkin-dependencies) dependency links.
However, running the job will improve performance of dependencies
queries.

#### Cassandra
The [Cassandra v1](zipkin-storage/cassandra-v1) component uses Cassandra
2.2+ features, but is tested against the latest patch of Cassandra 3.11.

The CQL was written in 2015, based on the original Cassandra schema from
Twitter, and since been extended. Spans are stored as opaque thrifts,
which means you cannot query fields in cqlsh. The schema was designed
for scale, including manually implemented indexes to make querying
larger data more performant.

Note: This store requires a [job to aggregate](https://github.com/openzipkin/zipkin-dependencies) dependency links.

## Running the server from source
The [Zipkin server](zipkin-server) receives spans via HTTP POST and respond to queries
from its UI. It can also run collectors, such as RabbitMQ or Kafka.

To run the server from the currently checked out source, enter the
following. JDK 8 is required.
```bash
# Build the server and also make its dependencies
$ ./mvnw -DskipTests --also-make -pl zipkin-server clean install
# Run the server
$ java -jar ./zipkin-server/target/zipkin-server-*exec.jar
```

## Artifacts
Server artifacts are under the maven group id `io.zipkin`
Library artifacts are under the maven group id `io.zipkin.zipkin2`

### Library Releases
Releases are uploaded to [Bintray](https://bintray.com/openzipkin/maven/zipkin) and synchronized to [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.zipkin%22)
### Library Snapshots
Snapshots are uploaded to [JFrog](http://oss.jfrog.org/artifactory/oss-snapshot-local) after commits to master.
### Docker Images
Released versions of zipkin-server are published to Docker Hub as `openzipkin/zipkin`.
See [docker-zipkin](https://github.com/openzipkin/docker-zipkin) for details.
### Javadocs
http://zipkin.io/zipkin contains versioned folders with JavaDocs published on each (non-PR) build, as well
as releases.
