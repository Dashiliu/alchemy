package com.dfire.platform.web.descriptor;

/**
 * @author congbai
 * @date 2018/6/8
 */
public class JarInfoDescriptor extends BasicDescriptor {

    private String clusterName;

    private String jarPath;

    private Integer parallelism;

    private String[] programArgs;

    private String entryClass;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public Integer getParallelism() {
        return parallelism;
    }

    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
    }

    public String[] getProgramArgs() {
        return programArgs;
    }

    public void setProgramArgs(String[] programArgs) {
        this.programArgs = programArgs;
    }

    public String getEntryClass() {
        return entryClass;
    }

    public void setEntryClass(String entryClass) {
        this.entryClass = entryClass;
    }

    @Override
    public void validate() throws Exception {
        //// TODO: 2018/6/8  
    }
}
