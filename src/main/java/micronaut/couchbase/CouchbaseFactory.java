package micronaut.couchbase;

import com.couchbase.client.java.*;
import io.micronaut.context.annotation.Factory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Example of configuring Couchbase directly in a non generic way.
 * Most of strings here should be settings that are loaded from properties using ConfigurationProperties.
 */
@Factory
public class CouchbaseFactory {

    private @Inject CouchbaseConfiguration configuration;

    @Singleton
    Cluster cluster() {
        return Cluster.connect(configuration.getHost(), configuration.getUsername(), configuration.getPassword());
    }

    @Singleton
    ReactiveCluster reactiveCluster(Cluster cluster) {
        return cluster.reactive();
    }

    @Singleton @Named("default")
    ReactiveBucket reactiveDefaultBucket(ReactiveCluster cluster) {
        return cluster.bucket(configuration.getDefaultBucketName());
    }

    @Singleton @Named("default")
    ReactiveCollection reactiveDefaultCollection( @Named("default") ReactiveBucket bucket) {
        return bucket.defaultCollection();
    }
}