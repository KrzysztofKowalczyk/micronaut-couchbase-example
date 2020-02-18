package micronaut.couchbase;

import com.couchbase.client.java.*;
import com.couchbase.client.java.json.JsonObject;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.PropertySource;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class require docker to run.
 * It is using test containers to setup Couchbase instance in docker image.
 * Then it uses Micronaut to inject Couchbase collection as constructed by {@link CouchbaseFactory}
 * and configured by {@link CouchbaseConfiguration}.
 *
 * Configuration can be passed by configuration files, system properties, env variables or explicitly on test.
 *
 * couchbase.username - provided explicitly
 * couchbase.password - provided by explicitly point to config file
 * couchbase.host - default value on configuration interface
 * couchbase.default-bucket-name - provided automatically by env specific properties file: application-test.yaml
 *
 * Every file can be defined using yaml, json or .property format. See https://docs.micronaut.io/latest/guide/index.html#propertySource
 */
@MicronautTest(propertySources = "classpath:extraProperties.yml")
@Property(name = "couchbase.username", value = "Administrator")
public class MicronautCouchbaseTest extends AbstractCouchbaseTest {

    @Inject @Named("default") ReactiveCollection defaultCollection;

    String testId = UUID.randomUUID().toString();

    @Test
    public void test_that_docker_image_works_with_async_api() throws InterruptedException {
        // given - test json document
        JsonObject jsonObject = JsonObject.create().put("content", "some other data");

        // when - new document is inserted
        defaultCollection.insert(testId, jsonObject).block();

        // then - inserted document can be retrieved by key
        JsonObject document = defaultCollection.get(testId).block().contentAsObject();
        assertThat(document.get("content")).isEqualTo("some other data");
    }
}
