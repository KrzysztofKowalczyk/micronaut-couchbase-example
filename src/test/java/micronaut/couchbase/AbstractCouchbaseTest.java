package micronaut.couchbase;

import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;

// See https://www.testcontainers.org/test_framework_integration/manual_lifecycle_control/#singleton-containers
public abstract class AbstractCouchbaseTest {
    static CouchbaseContainer couchbase;

    static {
        // Container created such way take some time to be killed, so you can't run test to quickly after killing previous run.
        couchbase = new CouchbaseContainer();
        couchbase.start();

        try {
            String configScript = null;

            // This script has hardcoded values for administrator name, password and creates one bucket "sampleBucket".
            URL stream = AbstractCouchbaseTest.class.getClassLoader().getResource("initCluster.sh");
            configScript = IOUtils.toString(stream, StandardCharsets.UTF_8);

            couchbase.runCommand("bash", "-c", configScript);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
