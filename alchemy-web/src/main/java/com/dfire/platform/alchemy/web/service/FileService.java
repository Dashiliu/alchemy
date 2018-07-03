package com.dfire.platform.alchemy.web.service;

import java.io.InputStream;

/**
 * @author congbai
 * @date 2018/7/2
 */
public interface FileService {

    void download(String filePath,String remoteUrl);

    String upload(String fileName,InputStream inputStream);

}
