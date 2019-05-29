package com.dfire.platform.alchemy.web.descriptor;

import java.util.*;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class DescriptorFactory {

    public static final DescriptorFactory me = new DescriptorFactory();

    private final Map<String, List<Descriptor>> typeDescriptors;

    private DescriptorFactory() {
        this.typeDescriptors = new HashedMap();
        ServiceLoader<Descriptor> serviceLoader = ServiceLoader.load(Descriptor.class);
        Iterator<Descriptor> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            Descriptor descriptor = iterator.next();
            if (StringUtils.isEmpty(descriptor.type())) {
                continue;
            }
            List<Descriptor> descriptorList = this.typeDescriptors.get(descriptor.type());
            if (descriptorList == null) {
                descriptorList = new ArrayList<>();
                this.typeDescriptors.put(descriptor.type(), descriptorList);
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
            if (clazz.isAssignableFrom(descriptor.getClass())) {
                return (T)descriptor;
            }
        }
        return null;
    }

}
