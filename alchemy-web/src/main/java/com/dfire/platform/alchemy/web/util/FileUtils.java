package com.dfire.platform.alchemy.web.util;

import org.apache.flink.client.program.JobWithJars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author congbai
 * @date 2018/7/1
 */
public class FileUtils {

    public static File uploadFile(byte[] file, String filePath, String fileName) {
        File target = new File(filePath);
        if (!target.exists()) {
            target.mkdirs();
        }
        File result = new File(filePath + fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(result);
            out.write(file);
            out.flush();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

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

    public static List<URL> createPath(List<String> avgs) throws MalformedURLException {
        List<URL> jarFiles = new ArrayList<>(avgs.size());
        for (String avg : avgs){
            try {
                URL jarFileUrl =  MavenJarUtils.forAvg(avg).getJarFile().getAbsoluteFile().toURI().toURL();
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
