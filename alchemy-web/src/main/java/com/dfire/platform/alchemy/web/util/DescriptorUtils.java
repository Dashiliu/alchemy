package com.dfire.platform.alchemy.web.util;

import com.dfire.platform.alchemy.web.descriptor.Descriptor;
import com.dfire.platform.alchemy.web.descriptor.DescriptorManager;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class DescriptorUtils {

    public static <T extends Descriptor> T find(String type, Class<T> clazz) {
        DescriptorManager descriptorManager = SpringUtils.getBean(DescriptorManager.class);
        if (descriptorManager == null) {
            return null;
        }
        return descriptorManager.find(type, clazz);
    }

}
