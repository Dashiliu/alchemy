package com.dfire.platform.web.service;

import com.dfire.platform.web.service.dto.JobFailMsgDTO;
import com.dfire.platform.web.web.vm.JobFailVM;

/**
 * @author congbai
 * @date 2018/6/8
 */
public interface JobFailService {

    void save(JobFailVM jobFailVM);

    JobFailMsgDTO find(Long acJobId);

}
