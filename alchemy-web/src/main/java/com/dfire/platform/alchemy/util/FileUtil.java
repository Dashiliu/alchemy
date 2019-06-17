package com.dfire.platform.alchemy.util;

import org.apache.flink.client.program.JobWithJars;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author congbai
 * @date 2018/7/1
 */
public class FileUtil {

    public static List<URL> createPath(File file) {
        List<URL> jarFiles = new ArrayList<>(1);
        if (file == null) {
            return jarFiles;
        }
        try {

            URL jarFileUrl = file.getAbsoluteFile().toURI().toURL();
            jarFiles.add(jarFileUrl);
            JobWithJars.checkJarFile(jarFileUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("JAR file is invalid '" + file.getAbsolutePath() + "'", e);
        } catch (IOException e) {
            throw new RuntimeException("Problem with jar file " + file.getAbsolutePath(), e);
        }
        return jarFiles;
    }

    public static List<URL> createPath(List<String> avgs, boolean cache) throws MalformedURLException {
        if (CollectionUtils.isEmpty(avgs)){
            return new ArrayList<>(0);
        }
        List<URL> jarFiles = new ArrayList<>(avgs.size());
        for (String avg : avgs){
            try {
                URL jarFileUrl =  MavenJarUtil.forAvg(avg, cache).getJarFile().getAbsoluteFile().toURI().toURL();
                jarFiles.add(jarFileUrl);
                JobWithJars.checkJarFile(jarFileUrl);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("avg is invalid '" +avg + "'", e);
            } catch (IOException e) {
                throw new RuntimeException("Problem with avg " + avg, e);
            }
        }
        return jarFiles;
    }

}
