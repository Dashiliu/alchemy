package com.dfire.platform.alchemy.web.cluster.flink;

import org.springframework.util.Assert;

import com.dfire.platform.alchemy.web.descriptor.JarInfoDescriptor;

/**
 * @author congbai
 * @date 04/06/2018
 */
public class JarSubmitFlinkRequest extends AbstractSubmitRequest {

    private JarInfoDescriptor jarInfoDescriptor;

    public JarInfoDescriptor getJarInfoDescriptor() {
        return jarInfoDescriptor;
    }

    public void setJarInfoDescriptor(JarInfoDescriptor jarInfoDescriptor) {
        this.jarInfoDescriptor = jarInfoDescriptor;
    }

    @Override
    public void validate() throws Exception {
        Assert.notNull(jarInfoDescriptor, "jar配置不能为空");
        jarInfoDescriptor.validate();
    }
}
