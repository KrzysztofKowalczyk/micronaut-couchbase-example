package micronaut.couchbase;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

/**
 * Immutable configuration using Micronaut configuration properties.
 * See https://docs.micronaut.io/latest/guide/index.html#immutableConfig
 */
@ConfigurationProperties("couchbase")
public interface CouchbaseConfiguration {

    @Bindable(defaultValue = "127.0.0.1")
    String getHost();
    String getUsername();
    String getPassword();
    String getDefaultBucketName();
}
