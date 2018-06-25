package com.dfire.platform.alchemy.web.service;

/**
 * 将job提交到cluster
 *
 * @author congbai
 * @date 2018/6/19
 */
public interface ClusterJobService {

    void submit(Long id);

}
