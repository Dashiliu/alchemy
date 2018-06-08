package com.dfire.platform.web.descriptor;

import org.springframework.util.Assert;

import com.dfire.platform.web.common.Table;
import com.dfire.platform.web.common.TimeAttribute;

/**
 * @author congbai
 * @date 02/06/2018
 */
public abstract class SourceDescriptor implements Descriptor {

    private String name;

    private TimeAttribute timeAttribute;

    private Table input;

    private Table output;

    @Override
    public void validate() throws Exception {
        Assert.notNull(name, "table的名称不能为空");
        Assert.notNull(output, "table字段不能为空");
        Assert.notEmpty(output.getFields(), "table字段不能为空");
        Assert.notNull(timeAttribute, "table时间属性不能为空");
        Assert.hasText(timeAttribute.getAttribute(), "table时间属性不能为空");
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TimeAttribute getTimeAttribute() {
        return timeAttribute;
    }

    public void setTimeAttribute(TimeAttribute timeAttribute) {
        this.timeAttribute = timeAttribute;
    }

    public Table getInput() {
        return input;
    }

    public void setInput(Table input) {
        this.input = input;
    }

    public Table getOutput() {
        return output;
    }

    public void setOutput(Table output) {
        this.output = output;
    }
}
