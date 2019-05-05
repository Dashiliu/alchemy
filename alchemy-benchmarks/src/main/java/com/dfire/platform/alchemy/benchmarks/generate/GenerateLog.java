package com.dfire.platform.alchemy.benchmarks.generate;

import java.io.Serializable;

/**
 * @author congbai
 * @date 2019/4/22
 */
public interface GenerateLog extends Serializable {

    byte[] generate();

}
