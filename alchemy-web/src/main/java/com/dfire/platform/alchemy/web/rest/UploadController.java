package com.dfire.platform.alchemy.web.rest;

import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.common.JarInfo;
import com.dfire.platform.alchemy.web.rest.util.HeaderUtil;
import com.dfire.platform.alchemy.web.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @author congbai
 * @date 2018/7/2
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);


    @PostMapping("/confs/{jobId}")
    public ResponseEntity<JarInfo> confs(@RequestParam("file") MultipartFile file, @PathVariable(value = "jobId") Long jobId) throws IOException {
        LOGGER.debug("REST request to upload  confs jar : {}", file.getOriginalFilename());
        if(!file.getOriginalFilename().endsWith("jar")){
            throw new RuntimeException("file must be a jar");
        }
        String fileName = jobId+file.getOriginalFilename();
        File uploadFile= FileUtils.uploadFile(file.getBytes(), Constants.FILE_PATH, fileName);
//        String remoteUrl=fileService.upload(fileName,file.getInputStream());
        JarInfo jarInfo=new JarInfo();
        jarInfo.setFileName(file.getOriginalFilename());
        jarInfo.setJarPath(uploadFile.getPath());
//        jarInfo.setRemoteUrl(remoteUrl);
        jarInfo.setUploadTime(new Date());
        return new ResponseEntity<>(jarInfo,
            HeaderUtil.createAlert("upload a jar ", null), HttpStatus.OK);
    }

    @PostMapping("/global")
    public ResponseEntity<JarInfo> global(@RequestParam("file") MultipartFile file) throws IOException {
        LOGGER.debug("REST request to upload jar global : {}", file.getOriginalFilename());
        if(!file.getOriginalFilename().endsWith("jar")){
            throw new RuntimeException("file must be a jar");
        }
        File uploadFile= FileUtils.uploadFile(file.getBytes(), Constants.FILE_PATH, Constants.GLOBAL_FILE_NAME);
        JarInfo jarInfo=new JarInfo();
        jarInfo.setFileName(Constants.GLOBAL_FILE_NAME);
        jarInfo.setJarPath(uploadFile.getPath());
        jarInfo.setUploadTime(new Date());
        return new ResponseEntity<>(jarInfo,
            HeaderUtil.createAlert("upload a jar ", null), HttpStatus.OK);
    }

    @GetMapping("/global")
    public ResponseEntity<JarInfo> get() throws IOException {
        LOGGER.debug("REST request to get global jar  : {}");
        File uploadFile= new File(Constants.FILE_PATH+Constants.GLOBAL_FILE_NAME);
        JarInfo jarInfo=null;
        if(uploadFile.exists()){
            jarInfo=new JarInfo();
            jarInfo.setFileName(Constants.GLOBAL_FILE_NAME);
            jarInfo.setJarPath(uploadFile.getPath());
            jarInfo.setUploadTime(new Date());
        }
        return new ResponseEntity<>(jarInfo,
            HeaderUtil.createAlert("upload a jar ", null), HttpStatus.OK);
    }

}
