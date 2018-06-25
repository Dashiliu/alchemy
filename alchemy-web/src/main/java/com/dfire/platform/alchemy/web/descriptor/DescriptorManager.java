package com.dfire.platform.alchemy.web.descriptor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

/**
 * @author congbai
 * @date 2018/6/19
 */
@Component
public class DescriptorManager {

    private final Map<String, Descriptor> descriptors;

    public DescriptorManager(Descriptor... descriptors) {
        Preconditions.checkNotNull(descriptors, "descriptors can't be null");
        this.descriptors = new HashMap<>(descriptors.length);
        for (Descriptor descriptor : descriptors) {
            this.descriptors.put(descriptor.getContentType(), descriptor);
        }
    }

    public Descriptor getByContentType(String contentType) {
        return this.descriptors.get(contentType);
    }

}
