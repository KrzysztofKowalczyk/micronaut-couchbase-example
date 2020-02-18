package micronaut.couchbase;

import com.couchbase.client.java.*;
import com.couchbase.client.java.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This class require docker to run.
 * It is using test containers to setup Couchbase instance in docker image.
 * Then it manually configures Couchbase SDK to connect to this cluster.
 */
public class DirectCouchbaseTest extends AbstractCouchbaseTest {

    @Test
    public void test_that_docker_image_works_with_blocking_api() throws InterruptedException {
        // given - test cluster with "sampleBucket" bucket
        Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
        Bucket bucket = cluster.bucket("sampleBucket");
        Collection collection = bucket.defaultCollection(); // SDK 3 and new versions of couchbase has concept of collections and scopes

        // when
        collection.insert("document", JsonObject.create().put("content", "some data"));

        // then
        assertThat(collection.get("document").contentAsObject().get("content")).isEqualTo("some data");
    }

    @Test
    public void test_that_docker_image_works_with_async_api() throws InterruptedException {
        // given - test cluster with "sampleBucket" bucket
        ReactiveCluster cluster = Cluster.connect( "127.0.0.1", "Administrator", "password").reactive();
        ReactiveBucket bucket = cluster.bucket("sampleBucket");
        ReactiveCollection collection = bucket.defaultCollection();

        // when - new document is inserted
        collection
            .insert(
                "document1", // as we share same cluster here, we need to use new document key
                JsonObject.create().put("content", "some other data")
            )
            .block()
        ;


        // then - inserted document can be retrieved by key
        JsonObject document = collection.get("document1").block().contentAsObject();
        assertThat(document.get("content")).isEqualTo("some other data");
    }
}
