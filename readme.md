# Example of using Couchbase in Micronaut

This project requires Java and docker to be available.

```sh
./gradlew test
```

To run tests that show the usage.

## Direct couchbase usage (no micronaut)

First have a look at [DirectCouchbaseTest](src/test/java/micronaut/couchbase/DirectCouchbaseTest.java) which configures couchbase client manually. Couchbase SDK went through complete rewrite betwee 2.x and 3.x branches, hence don't commit to old one.

This class extends a [AbstractCouchbaseTest](src/test/java/micronaut/couchbase/AbstractCouchbaseTest.java) which setup a whole instance of Couchbase running inside docker image. It is using https://www.testcontainers.org/ but I am not reusing existing Couchbase image as it clashed with Micronaut and/or new SDK. It created an instance with different values hardcoded in [initCluster.sh](src/test/resources/initCluster.sh).


## Using micronaut to configure and inject Couchbase components

In this case the setup of Couchbase is done more in Micronaut-way, but still using custom defined classes.

Eventually you can simply inject Couchbase Collection like it is done in 
[MicronautCouchbaseTest](src/test/java/micronaut/couchbase/MicronautCouchbaseTest.java#L36)

Couchbase components are provided for injection in [CouchbaseFactory](src/main/java/micronaut/couchbase/CouchbaseFactory.java), which is normal Micronaut dependency injection.

Configuration is provided through static and immutable way using [CouchbaseConfiguration](src/main/java/micronaut/couchbase/CouchbaseConfiguration.java) interface.
This interface takes properties according to Micronaut convetion mapping from multiple sources.

In this example those are 4 different sources:
 * couchbase.username - provided explicitly using @Property annotation on the test class
 * couchbase.password - provided by explicitly by point to config file
 * couchbase.host - default value defined on CouchbaseConfiguration
 * couchbase.default-bucket-name - provided automatically by env specific properties file: application-test.yaml
 
File sources can be defined using yaml, json or .property format. See [Micronaut propertySource](https://docs.micronaut.io/latest/guide/index.html#propertySource) for details.
