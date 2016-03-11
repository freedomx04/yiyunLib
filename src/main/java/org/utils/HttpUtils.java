package org.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtils {

    static Log log = LogFactory.getLog(HttpUtils.class);

    public static String getResponseAsString(String url) throws IOException {
        String ret = "";
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpGet method = new HttpGet(url);
        
        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() < 400) {
            ret = EntityUtils.toString(entity, "UTF-8");
        } else {
            EntityUtils.consume(entity);
        }
        client.close();
        return ret;
    }

    public static byte[] getResponseBody(String url) throws IOException {
        byte[] content = new byte[0];
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpGet method = new HttpGet(url);
        
        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() < 400) {
            content = EntityUtils.toByteArray(entity);
        } else {
            EntityUtils.consume(entity);
        }
        client.close();
        return content;
    }

    public static String getResponseAsString(String url, HashMap<String, String> map) throws IOException {
        String ret = "";
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpPost method = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (String key : map.keySet()) {
            params.add(new BasicNameValuePair(key, map.get(key)));
        }
        method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() < 400) {
            ret = EntityUtils.toString(entity, "UTF-8");
        } else {
            EntityUtils.consume(entity);
        }
        client.close();
        return ret;
    }
    
    public static String getResponseAsString(String url, HashMap<String, String> map, HashMap<String, String> headers) throws IOException {
        String ret = "";
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpPost method = new HttpPost(url);
        
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (String key : map.keySet()) {
            params.add(new BasicNameValuePair(key, map.get(key)));
        }
        method.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        
        for (String key: headers.keySet()) {
            method.addHeader(key, headers.get(key));
        }

        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() < 400) {
            ret = EntityUtils.toString(entity, "UTF-8");
        } else {
            EntityUtils.consume(entity);
        }
        client.close();
        return ret;
    }

    public static boolean downloadFile(String url, File output) throws IOException {
        boolean ret = false;
        
        FileUtils.sureDirExists(output, true);
        
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpGet method = new HttpGet(url);
        CloseableHttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() < 400) {
            InputStream is = entity.getContent();
            FileOutputStream fos = new FileOutputStream(output);
            try {
                IOUtils.copy(is, fos);
                ret = true;
            } catch (IOException e) {
                log.debug(e, e);
            } finally {
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
                if (is != null) {
                    is.close();
                    is = null;
                }
            }
        } else {
            EntityUtils.consume(entity);
        }
        client.close();
        
        if (!ret) {
            output.delete();
        }
        return ret;
    }
}
