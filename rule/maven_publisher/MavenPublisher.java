package top.fifthlight.fabazel.mavenpublisher;

import javax.inject.Inject;
import javax.inject.Named;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.sisu.Parameters;
import org.eclipse.sisu.launch.Main;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Named
public class MavenPublisher {
    @Inject
    private RepositorySystem repositorySystem;

    @Inject
    @Parameters
    private String[] args;

    public int run() {
        var cli = new Cli(repositorySystem);
        return new CommandLine(cli).execute(args);
    }

    public static void main(String[] args) {
        var root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        var publisher = Main.boot(MavenPublisher.class, args);
        System.exit(publisher.run());
    }

    @Command(name = "maven-publisher", mixinStandardHelpOptions = true, description = "Publishes artifacts to a Maven repository")
    private static class Cli implements Callable<Integer> {
        private final RepositorySystem repositorySystem;

        @Option(names = {"--groupId"}, description = "Maven coordinate groupId", required = true)
        private String groupId;

        @Option(names = {"--artifactId"}, description = "Maven coordinate artifactId", required = true)
        private String artifactId;

        @Option(names = {"--version"}, description = "Maven coordinate version", required = true)
        private String version;

        @Option(names = {"--artifact"}, description = "Artifact in format 'classifier:extension=file_path' (classifier can be empty for main artifact)", required = true)
        private List<String> artifacts = new ArrayList<>();

        @Option(names = {"--pom"}, description = "POM file path")
        private Path pomFile;

        private Cli(RepositorySystem repositorySystem) {
            this.repositorySystem = repositorySystem;
        }

        private RemoteRepository createRepository(String url, String username, String password) {
            var builder = new RemoteRepository.Builder("remote", "default", url);
            var auth = new AuthenticationBuilder();
            if (username != null) {
                auth.addUsername(username);
            }
            if (password != null) {
                auth.addPassword(password);
            }
            builder.setAuthentication(auth.build());
            return builder.build();
        }

        @Override
        public Integer call() {
            var repoUrl = System.getenv("MAVEN_REPO_URL");
            if (repoUrl == null || repoUrl.isEmpty()) {
                System.err.println("Error: MAVEN_REPO_URL environment variable is required");
                return 1;
            }

            var username = System.getenv("MAVEN_USER");
            var password = System.getenv("MAVEN_PASSWORD");

            var localRepoDirectory = Path.of(System.getProperty("user.home"), ".m2", "repository");
            var session = repositorySystem.createSessionBuilder()
                    .withLocalRepositoryBaseDirectories(localRepoDirectory)
                    .build();
            var repository = createRepository(repoUrl, username, password);

            var aetherArtifacts = new ArrayList<Artifact>();

            if (pomFile != null) {
                var pomArtifact = new DefaultArtifact(groupId, artifactId, "pom", version).setPath(pomFile);
                aetherArtifacts.add(pomArtifact);
            }

            for (var artifactSpec : artifacts) {
                var parts = artifactSpec.split("=", 2);
                if (parts.length != 2) {
                    System.err.println("Error: Invalid artifact format: " + artifactSpec + ". Expected 'classifier:extension=file_path'");
                    return 1;
                }

                var classifierExt = parts[0];
                var filePath = parts[1];

                var classifierParts = classifierExt.split(":", 2);
                var classifier = "";
                var extension = "jar";

                if (classifierParts.length == 2) {
                    classifier = classifierParts[0];
                    extension = classifierParts[1];
                } else if (classifierParts.length == 1) {
                    if (!classifierParts[0].isEmpty()) {
                        extension = classifierParts[0];
                    }
                }

                var artifactPath = Path.of(filePath);
                if (!artifactPath.toFile().exists()) {
                    System.err.println("Error: Artifact file does not exist: " + filePath);
                    return 1;
                }

                var artifact = new DefaultArtifact(groupId, artifactId, classifier, extension, version).setPath(artifactPath);
                aetherArtifacts.add(artifact);
            }

            var deployRequest = new DeployRequest();
            deployRequest.setRepository(repository);
            deployRequest.setArtifacts(aetherArtifacts);

            try {
                repositorySystem.deploy(session, deployRequest);
                System.out.println("Successfully deployed " + aetherArtifacts.size() + " artifact(s) to: " + repoUrl);
                for (var artifact : aetherArtifacts) {
                    var classifier = artifact.getClassifier();
                    var extension = artifact.getExtension();
                    var coord = new StringBuilder();
                    coord.append(groupId).append(":").append(artifactId);
                    if (!classifier.isEmpty()) {
                        coord.append(":").append(classifier);
                    }
                    coord.append(":").append(version).append(".").append(extension);
                    coord.append(" (").append(extension);
                    if (!classifier.isEmpty()) {
                        coord.append(", ").append(classifier);
                    }
                    coord.append(")");
                    System.out.println("  - " + coord);
                }
                return 0;
            } catch (Exception e) {
                System.err.println("Error deploying artifacts: " + e.getMessage());
                e.printStackTrace(System.err);
                return 1;
            }
        }
    }
}
