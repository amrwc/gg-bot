package dev.amrw.runner.chain.run;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import dev.amrw.runner.config.Config;
import dev.amrw.runner.dto.RunArgs;
import dev.amrw.runner.helper.DockerClientHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.chain.impl.ContextBase;

/**
 * Context of the Run Chain.
 */
@Getter
public class RunChainContext extends ContextBase {

    /** Path where Gradle puts the application's JAR file onto. */
    public static final String GRADLE_LIBS_PATH = "/home/gradle/project/build/libs";
    /** Name of the tar.gz archive with {@link #GRADLE_LIBS_PATH} contents to be copied onto the host machine. */
    public static final String GRADLE_LIBS_TAR_GZ_ARCHIVE_NAME = "gradle-libs.tar.gz";
    /** Path to the host directory where things can be copied into. */
    public static final String HOST_BIN_PATH = "./bin";
    /** Path to the unarchived Gradle {@code libs} directory. */
    public static final String HOST_BIN_LIBS_PATH = HOST_BIN_PATH + "/libs";
    /** Path to the host location of Gradle {@code libs} archive. */
    public static final String HOST_GRADLE_LIBS_ARCHIVE_PATH = HOST_BIN_PATH + "/" + GRADLE_LIBS_TAR_GZ_ARCHIVE_NAME;
    /** Name of the application JAR file. */
    public static final String APP_JAR_FILENAME = "app.jar";
    /** Path to the application JAR file on the host machine. */
    public static final String HOST_APP_JAR_PATH = HOST_BIN_PATH + "/" + APP_JAR_FILENAME;
    /** Path to projects directory on the main (runtime) Docker image. */
    public static final String REMOTE_PROJECT_PATH = "/home/project";

    private final Config config;
    private final RunArgs args;
    private final DockerClient dockerClient;
    private final DockerClientHelper dockerClientHelper;

    // TODO: Rethink this approach of passing state between commands. Is there a cleaner way of passing certain
    //  information between steps that don't necessarily happen one after the other? What if a step must run
    //  conditionally on one of these properties? Perhaps it's better to maintain a Set of steps to skip and add items
    //  to it depending on the circumstances in the current step?
    //  <p>
    //  OK, so they're only populated in the first command of the chain. Maybe I should enclose them in some sort of a
    //  pre-chain POJO? This way they'll be immutable. Then, things such as `buildContainerId` can be mutable and stored
    //  here as a field.
    @Setter
    @Accessors(fluent = true)
    private boolean networkExists;
    @Setter
    @Accessors(fluent = true)
    private boolean buildCacheVolumeExists;
    @Setter
    @Accessors(fluent = true)
    private boolean buildImageExists;
    @Setter
    @Accessors(fluent = true)
    private boolean buildContainerExists;
    @Setter
    @Accessors(fluent = true)
    private boolean mainImageExists;
    @Setter
    @Accessors(fluent = true)
    private boolean mainContainerExists;

    public RunChainContext(final Config config, final RunArgs args) {
        super();
        this.args = args;
        this.config = config;
        final var dockerClientConfig = DefaultDockerClientConfig
                .createDefaultConfigBuilder()
                .build();
        final var httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(dockerClientConfig.getDockerHost())
                .sslConfig(dockerClientConfig.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(dockerClientConfig, httpClient);
        this.dockerClientHelper = new DockerClientHelper(this.dockerClient);
    }

    public String getBuildImageName() {
        return config
                .getDockerConfig()
                .getBuildImageConfig()
                .getName();
    }

    public String getMainImageName() {
        return config
                .getDockerConfig()
                .getMainImageConfig()
                .getName();
    }
}
