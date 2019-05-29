package com.dfire.platform.alchemy.web.service.impl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.dfire.platform.alchemy.web.common.Constants;
import com.dfire.platform.alchemy.web.service.FileService;
import com.dfire.platform.alchemy.web.util.JsonUtils;

/**
 * @author congbai
 * @date 2018/7/2
 */
@Component
@ConfigurationProperties(prefix = "alchemy.file")
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileServiceImpl.class);

    private final CloseableHttpClient httpClient;

    private String uploadUrl;

    private String downloadUrl;

    public FileServiceImpl() throws Exception {
        this.httpClient = createHttpClient();
    }

    private CloseableHttpClient createHttpClient()
        throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(Constants.SO_TIMEOUT)
            .setConnectTimeout(Constants.CONNECTION_TIMEOUT)
            .setConnectionRequestTimeout(Constants.CONNECTION_REQUEST_TIMEOUT).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(requestConfig)
            .setKeepAliveStrategy(new HttpKeepAliveStrategy()).build();
    }

    public String upload(String fileName, InputStream inputStream) {
        CloseableHttpResponse response = null;
        HttpPost post = null;
        try {
            post = new HttpPost(uploadUrl);
            HttpEntity entity = createUploadEntity(fileName, inputStream);
            post.setEntity(entity);
            response = httpClient.execute(post);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity());
                LOGGER.info("upload success,result:{}", result);
                HttpResult httpResult = JsonUtils.fromJson(result, HttpResult.class);
                if (httpResult.code == 1) {
                    return httpResult.getData();
                } else {
                    LOGGER.warn("upload fail,result:{}", result);
                }
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (ClientProtocolException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (response != null) {
                post.releaseConnection();
            }

            try {
                if (response != null) {
                    response.close();
                }

            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return null;
    }

    public void download(String filePath, String remoteUrl) {
        File file = new File(filePath);
        if (file.exists()) {
            return;
        }
        FileOutputStream fops = null;
        try {
            fops = new FileOutputStream(file);
            URL url = new URL(downloadUrl + remoteUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
            InputStream inStream = conn.getInputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                fops.write(buffer, 0, len);
            }
            fops.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fops != null) {
                try {
                    fops.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private HttpEntity createUploadEntity(String fileName, InputStream inputStream)
        throws UnsupportedEncodingException {
        MultipartEntity entity
            = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
        entity.addPart("file", new InputStreamBody(inputStream, "multipart/form-data", fileName));
        entity.addPart("projectName", new StringBody("OssAssets", Charset.forName("UTF-8")));
        entity.addPart("usrName", new StringBody("congbai", Charset.forName("UTF-8")));
        entity.addPart("path", new StringBody("alchemy", Charset.forName("UTF-8")));
        return entity;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    static class HttpResult {

        private int code;
        private String message;
        private String data;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }

    static class HttpKeepAliveStrategy implements ConnectionKeepAliveStrategy {
        /**
         * 取不到服务器返回的keep-alive值,设置一个默认的keepAlive时间
         */
        private int keepAlive = Constants.KEEP_ALIVE;

        @Override
        public long getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
            Args.notNull(response, "HTTP response");
            final HeaderElementIterator it
                = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                final HeaderElement he = it.nextElement();
                final String param = he.getName();
                final String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (final NumberFormatException ignore) {
                    }
                }
            }
            if (keepAlive > 0) {
                return keepAlive;
            }
            return -1;
        }

    }
}
