package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.api.function.BaseFunction;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @author congbai
 * @date 2018/6/19
 */
public class FunctionFactory {

    public static final FunctionFactory me = new FunctionFactory();

    private final Map<String, BaseFunction> baseFunctionMap;

    private FunctionFactory() {
        this.baseFunctionMap = new HashedMap();
        ServiceLoader<BaseFunction> serviceLoader = ServiceLoader.load(BaseFunction.class);
        Iterator<BaseFunction> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            BaseFunction function = iterator.next();
            if (StringUtils.isEmpty(function.getFunctionName())) {
                continue;
            }
            this.baseFunctionMap.put(function.getFunctionName(), function);
        }
    }

    public Map<String, BaseFunction> getBaseFunctionMap() {
        return baseFunctionMap;
    }

    public <T extends BaseFunction> T find(String name, Class<T> clazz) {
        BaseFunction function = this.baseFunctionMap.get(name);
        if (function == null) {
            return null;
        }
        if (clazz.isAssignableFrom(function.getClass())) {
            return (T) function;
        }
        return null;
    }

}
