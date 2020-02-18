package micronaut.couchbase;

import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.shaded.com.google.common.collect.Sets;
import java.util.Set;

/**
 * Quick and dirty container just to get it working, existing test container is quite problematic as it is using old version of Couchbase SDK.
 * It can break because of classpath clashes with new SDK, Micronaut or others.
 *
 * It use fixed ports which requires those port to be available on host.
 * One could do that using mapped ports, but then you are required to configure client with them dynamically.
 */
public class CouchbaseContainer extends FixedHostPortGenericContainer<CouchbaseContainer> {

    public CouchbaseContainer() {
        super("couchbase/server:6.0.0");

        withNetwork(Network.SHARED);

        withFixedExposedPort(8091, 8091); // rest port
        withFixedExposedPort(8092, 8092); // capi_port
        withFixedExposedPort(8093, 8093); // query_port
        withFixedExposedPort(8094, 8094); // fts_http_port
        withFixedExposedPort(8095, 8095); // cbas_http_port
        withFixedExposedPort(8096, 8096); // eventing_http_port
        withFixedExposedPort(11207, 11207); // memcached_ssl_port
        withFixedExposedPort(11210, 11210); // memcached_port
        withFixedExposedPort(18091, 18091); // ssl_rest_port
        withFixedExposedPort(18092, 18092); // ssl_capi_port
        withFixedExposedPort(18093, 18093); // ssl_query_port
        withFixedExposedPort(18094, 18094); // fts_ssl_port
        withFixedExposedPort(18095, 18095); // cbas_ssl_port
        withFixedExposedPort(18096, 18096); // eventing_ssl_port

        waitingFor(new HttpWaitStrategy().forPort(8091).forPath("/ui/index.html"));
    }

    public Set<Integer> getLivenessCheckPortNumbers() {
        return Sets.newHashSet(8091);
    }

    /**
     * Runs command against docker image, it pipes std out and std err in real time and fails if return code of execution is not 0.
     */
    public void runCommand(String... command) throws InterruptedException {
        ExecCreateCmd cmd = dockerClient.execCreateCmd(getContainerId())
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withCmd(command);

        String executionId = cmd.exec().getId();

        dockerClient.execStartCmd(executionId)
                .withDetach(false)
                .exec(createExecCallback())
                .awaitCompletion();

        InspectExecResponse lastExecResponse = dockerClient.inspectExecCmd(executionId).exec();

        if(!lastExecResponse.getExitCode().equals(0)) {
            throw new RuntimeException("Docker command has failed");
        }
    }

    ExecStartResultCallback createExecCallback() {
        return new ExecStartResultCallback(System.out, System.err);
    }
}
