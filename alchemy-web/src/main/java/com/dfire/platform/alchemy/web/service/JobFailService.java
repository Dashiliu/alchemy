package com.dfire.platform.alchemy.web.service;

import com.dfire.platform.alchemy.web.rest.vm.JobFailVM;
import com.dfire.platform.alchemy.web.service.dto.JobFailMsgDTO;

/**
 * @author congbai
 * @date 2018/6/8
 */
public interface JobFailService {

    void save(JobFailVM jobFailVM);

    JobFailMsgDTO find(Long acJobId);

}
