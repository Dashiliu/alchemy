package com.dfire.platform.alchemy.web.cluster.request;

import com.dfire.platform.alchemy.web.descriptor.JarInfoDescriptor;
import org.springframework.util.Assert;

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
