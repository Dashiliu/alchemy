package com.dfire.platform.alchemy.web.service;

import java.util.List;

import com.dfire.platform.alchemy.web.rest.vm.JobConfVM;
import com.dfire.platform.alchemy.web.service.dto.JobConfDTO;

/**
 * @author congbai
 * @date 2018/6/8
 */
public interface JobConfService {

    void save(JobConfVM jobConfVM);

    void update(JobConfVM jobConfVM);

    List<JobConfDTO> findByType(Long jobiD, int type);

    List<JobConfDTO> findByJobId(Long jobiD);

    List<JobConfDTO> findAll();

    void delete(Long id);
}
