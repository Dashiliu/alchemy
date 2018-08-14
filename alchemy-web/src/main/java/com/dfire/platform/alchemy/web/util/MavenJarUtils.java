package com.dfire.platform.alchemy.web.util;

import com.bigfatgun.MavenClassLoader;
import com.bigfatgun.MavenLoaderInfo;
import com.dfire.platform.alchemy.web.common.Constants;

import java.io.File;

/**
 * @author congbai
 * @date 2018/8/10
 */
public class MavenJarUtils {

    public static MavenLoaderInfo forAvg(String avg){
        return MavenClassLoader.forGAV(avg, Constants.RELEASE_REPOSITORY_URL,Constants.SNAP_REPOSITORY_URL);
    };

}
