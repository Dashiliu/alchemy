package com.dfire.platform.web.service;

import java.util.List;

import com.dfire.platform.web.service.dto.JobConfDTO;
import com.dfire.platform.web.web.vm.JobConfVM;

/**
 * @author congbai
 * @date 2018/6/8
 */
public interface JobConfService {

    void save(JobConfVM jobConfVM);

    void update(JobConfVM jobConfVM);

    List<JobConfDTO> findByType(int type);

    void delete(Long id);
}
