package com.dfire.platform.alchemy.web.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author congbai
 * @date 2018/7/1
 */
public class FileUtils {

    public static File uploadFile(byte[] file, String filePath, String fileName){
        File target = new File(filePath);
        if(!target.exists()){
            target.mkdirs();
        }
        File result=new File(filePath+fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(result);
            out.write(file);
            out.flush();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return result;
    }

}
