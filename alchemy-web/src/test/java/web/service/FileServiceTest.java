package web.service;

import java.io.File;
import java.io.FileInputStream;

import com.dfire.platform.alchemy.web.service.impl.FileServiceImpl;

/**
 * @author congbai
 * @date 2018/7/2
 */
public class FileServiceTest {

    public static void main(String[] args) throws Exception {
        FileServiceImpl fileService = new FileServiceImpl();
        fileService.setUploadUrl("http://upload.2dfire-daily.com/upbigfile");
        fileService.setDownloadUrl("https://assets.2dfire.com/");
        String remoteUrl = fileService.upload("test.jar", new FileInputStream(new File(
            "/Users/dongbinglin/Code/platform/alchemy/alchemy-connectors/target/original-alchemy-connectors-1.0-SNAPSHOT.jar")));
        fileService.download("/tmp/test.jar", remoteUrl);
    }

}
