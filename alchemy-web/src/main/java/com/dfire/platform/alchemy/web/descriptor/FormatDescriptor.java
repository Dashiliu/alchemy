package com.dfire.platform.alchemy.web.descriptor;

import com.dfire.platform.alchemy.web.common.Constants;

/**
 * @author congbai
 * @date 2018/6/30
 */
public class FormatDescriptor implements Descriptor {

    private String type;

    @Override
    public String getType() {
        return Constants.TYPE_VALUE_FORMAT;
    }

    @Override
    public void validate() throws Exception {

    }
}
