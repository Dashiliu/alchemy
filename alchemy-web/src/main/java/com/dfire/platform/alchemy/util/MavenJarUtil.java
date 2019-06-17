package com.dfire.platform.alchemy.util;

import com.dfire.platform.alchemy.common.MavenLoaderInfo;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.DependencyCollectionException;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.RepositoryPolicy;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.aether.util.graph.PreorderNodeListGenerator;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author congbai
 * @date 2018/8/10
 */
public class MavenJarUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlchemyProperties.class);

    private static final String KEY_RELEASE_REPOSITORY_URL = "release.url";

    private static final String KEY_SNAP_REPOSITORY_URL = "snapshot.url";

    private static  String RELEASE_REPOSITORY_URL;

    private static  String SNAP_REPOSITORY_URL ;

    static {
        try {
            File file = ResourceUtils.getFile("classpath:maven.properties");
            Properties properties = PropertiesUtil.create(file);
            RELEASE_REPOSITORY_URL = properties.getProperty(KEY_RELEASE_REPOSITORY_URL);
            SNAP_REPOSITORY_URL = properties.getProperty(KEY_SNAP_REPOSITORY_URL);
        } catch (IOException e) {
            LOGGER.error("Exception maven.properties", e);
        }
    }

    private static Map<String, MavenLoaderInfo>  caches = new ConcurrentHashMap<>();

    public static MavenLoaderInfo forAvg(String avg) {
        return forAvg(avg, false);
    }

    public static MavenLoaderInfo forAvg(String avg, boolean cache) {
        if(cache){
            MavenLoaderInfo mavenLoaderInfo = caches.get(avg);
            if(mavenLoaderInfo == null){
                mavenLoaderInfo =  MavenClassLoader.forGAV(avg, RELEASE_REPOSITORY_URL, SNAP_REPOSITORY_URL);
                caches.put(avg, mavenLoaderInfo);
            }
            return mavenLoaderInfo;
        }else{
            return  MavenClassLoader.forGAV(avg, RELEASE_REPOSITORY_URL, SNAP_REPOSITORY_URL);
        }

    }

    public static final class MavenClassLoader {
        public MavenClassLoader() {}

        public static MavenLoaderInfo forGAV(String gav, String releaseRepositoryUrl, String snapRepositoryUrl) {
            return usingCentralRepo(releaseRepositoryUrl, snapRepositoryUrl).forGAV(Preconditions.checkNotNull(gav));
        }

        public static MavenClassLoader.ClassLoaderBuilder usingCentralRepo(String releaseRepositoryUrl,
            String snapRepositoryUrl) {
            RemoteRepository snapRepository = new RemoteRepository("central", "default", snapRepositoryUrl);
            snapRepository.setPolicy(true, new RepositoryPolicy(true, "always", "warn"));
            RemoteRepository releaseRepository = new RemoteRepository("central", "default", releaseRepositoryUrl);
            return new MavenClassLoader.ClassLoaderBuilder(new RemoteRepository[] {snapRepository, releaseRepository});
        }

        public static class ClassLoaderBuilder {
            private static final String COMPILE_SCOPE = "compile";
            private static final ClassLoader SHARE_NOTHING = null;
            private final ImmutableList<RemoteRepository> repositories;
            private final File localRepositoryDirectory;

            private ClassLoaderBuilder(RemoteRepository... repositories) {
                Preconditions.checkNotNull(repositories);
                Preconditions.checkArgument(repositories.length > 0, "Must specify at least one remote repository.");
                this.repositories = ImmutableList.copyOf(repositories);
                this.localRepositoryDirectory = new File(System.getProperty("user.home") + "/.m2/repository");
            }

            public MavenLoaderInfo forGAV(String gav) {
                try {
                    CollectRequest e = this.createCollectRequestForGAV(gav);
                    List artifacts = this.collectDependenciesIntoArtifacts(e);
                    LinkedList urls = Lists.newLinkedList();
                    String[] gavs = gav.split(":");
                    String jarName = gavs[1] + "-" + gavs[2];
                    File targetFile = null;
                    Iterator urlClassLoader = artifacts.iterator();

                    while (urlClassLoader.hasNext()) {
                        Artifact artifact = (Artifact)urlClassLoader.next();
                        File file = artifact.getFile();
                        urls.add(file.toURI().toURL());
                        if (file.getName().contains(jarName)) {
                            targetFile = file;
                        }
                    }
                    URLClassLoader classLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
                        @Override
                        public URLClassLoader run() {
                            return new URLClassLoader((URL[])urls.toArray(new URL[urls.size()]), SHARE_NOTHING);
                        }
                    });
                    return new MavenLoaderInfo(classLoader, targetFile);
                } catch (Exception var11) {
                    throw Throwables.propagate(var11);
                }
            }

            private CollectRequest createCollectRequestForGAV(String gav) {
                Dependency dependency = new Dependency(new DefaultArtifact(gav), "compile");
                CollectRequest collectRequest = new CollectRequest();
                collectRequest.setRoot(dependency);
                Iterator i$ = this.repositories.iterator();

                while (i$.hasNext()) {
                    RemoteRepository repository = (RemoteRepository)i$.next();
                    collectRequest.addRepository(repository);
                }

                return collectRequest;
            }

            private List<Artifact> collectDependenciesIntoArtifacts(CollectRequest collectRequest)
                throws PlexusContainerException, ComponentLookupException, DependencyCollectionException,
                ArtifactResolutionException {
                RepositorySystem repositorySystem = this.newRepositorySystem();
                RepositorySystemSession session = this.newSession(repositorySystem);
                DependencyNode node = repositorySystem.collectDependencies(session, collectRequest).getRoot();
                repositorySystem.resolveDependencies(session, node, null);
                PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();
                node.accept(nlg);
                return nlg.getArtifacts(false);
            }

            private RepositorySystem newRepositorySystem() throws PlexusContainerException, ComponentLookupException {
                return new DefaultPlexusContainer().lookup(RepositorySystem.class);
            }

            private RepositorySystemSession newSession(RepositorySystem system) {
                MavenRepositorySystemSession session = new MavenRepositorySystemSession();
                LocalRepository localRepo = new LocalRepository(this.localRepositoryDirectory);
                session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));
                return session;
            }
        }
    }

}
