package com.dfire.platform.web.cluster.request;

import com.dfire.platform.web.descriptor.JarInfoDescriptor;

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
}
