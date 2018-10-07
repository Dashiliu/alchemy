package com.dfire.platform.alchemy.web.descriptor;

import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 2018/6/8
 */
public class JarInfoDescriptor implements Descriptor {

    private String avg;

    private Integer parallelism;

    private String[] programArgs;

    private String entryClass;

    public String getAvg() {
        return avg;
    }

    public void setAvg(String avg) {
        this.avg = avg;
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
    public String getType() {
        return Constants.TYPE_VALUE_JAR;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(avg, "maven包不能为空");
        Assert.notNull(parallelism, "并发数不能为空");
        Assert.notNull(entryClass, "main函数不能为空");
    }
}
