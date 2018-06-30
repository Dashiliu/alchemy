package com.dfire.platform.alchemy.web.descriptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Preconditions;

/**
 * @author congbai
 * @date 2018/6/19
 */
@Component
public class DescriptorManager {

    private final Map<String, List<Descriptor>> typeDescriptors;

    public DescriptorManager(Descriptor... descriptors) {
        Preconditions.checkNotNull(descriptors, "descriptors can't be null");
        this.typeDescriptors = new HashMap<>(descriptors.length);
        for (Descriptor descriptor : descriptors) {
            List<Descriptor> descriptorList = this.typeDescriptors.get(descriptor.getType());
            if (descriptorList == null) {
                descriptorList = new ArrayList<>();
                this.typeDescriptors.put(descriptor.getType(), descriptorList);
            }
            if (!descriptorList.contains(descriptor)) {
                descriptorList.add(descriptor);
            }
        }
    }

    public <T extends Descriptor> T find(String type, Class<T> clazz) {
        List<Descriptor> descriptorList = this.typeDescriptors.get(type);
        if (CollectionUtils.isEmpty(descriptorList)) {
            return null;
        }
        for (Descriptor descriptor : descriptorList) {
            if (descriptor.getClass().isAssignableFrom(clazz)) {
                return (T)descriptor;
            }
        }
        return null;
    }

}
