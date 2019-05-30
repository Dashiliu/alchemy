package com.dfire.platform.alchemy.web.maven;

import java.io.File;

import org.junit.Test;

import com.dfire.platform.alchemy.web.common.MavenLoaderInfo;
import com.dfire.platform.alchemy.web.util.MavenJarUtils;

/**
 * @author congbai
 * @date 2018/8/10
 */
public class MavenClassLoaderTest {

    @Test
    public void load() {
        MavenLoaderInfo mavenLoaderInfo = MavenJarUtils.forAvg("com.dfire.platform:mirror:0.0.1-daily-SNAPSHOT");
        File file = mavenLoaderInfo.getJarFile();
        assert file.exists();
    }

}
