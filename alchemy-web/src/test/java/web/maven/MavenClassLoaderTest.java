package web.maven;

import com.bigfatgun.MavenClassLoader;
import com.bigfatgun.MavenLoaderInfo;
import com.dfire.platform.alchemy.web.common.Constants;
import org.junit.Test;

import java.io.File;

/**
 * @author congbai
 * @date 2018/8/10
 */
public class MavenClassLoaderTest {

    @Test
    public void load(){
        MavenLoaderInfo mavenLoaderInfo=MavenClassLoader.forGAV("com.dfire.platform:sunset:0.0.1-SNAPSHOT", Constants.RELEASE_REPOSITORY_URL,Constants.SNAP_REPOSITORY_URL);
        File file=mavenLoaderInfo.getJarFile();
        assert  file.exists();
    }

}
