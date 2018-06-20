package com.dfire.platform.web.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dfire.platform.web.data.AcJobRepository;
import com.dfire.platform.web.data.AcServiceRepository;

/**
 * @author congbai
 * @date 2018/6/8
 */
@RestController
@RequestMapping("/job")
public class JobController {

    private final Logger log = LoggerFactory.getLogger(JobController.class);

    private final AcServiceRepository acServiceRepository;

    private final AcJobRepository acJobRepository;

    public JobController(AcServiceRepository acServiceRepository, AcJobRepository acJobRepository) {
        this.acServiceRepository = acServiceRepository;
        this.acJobRepository = acJobRepository;
    }

}
