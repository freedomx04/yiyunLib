package itec.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
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
import org.tsaikd.java.utils.WebClient;

public class HttpUtilsWithSession {
    
    static Log log = LogFactory.getLog(HttpUtilsWithSession.class);
    
    private Set<String> cookies = new TreeSet<String>();

    public String getResponseAsString(String url) throws IOException {
        String ret = "";
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpGet method = new HttpGet(url);
        for (String cookie : cookies) {
            method.addHeader("Cookie", cookie);
        }
        
        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() < 400) {
            ret = EntityUtils.toString(entity, "UTF-8");
            Header[] headers = response.getHeaders("Set-Cookie");
            for (Header h : headers) {
                cookies.add(h.getValue());
            }
        } else {
            EntityUtils.consume(entity);
        }
        client.close();
        return ret;
    }

    public byte[] getResponseBody(String url) throws IOException {
        byte[] content = new byte[0];
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpGet method = new HttpGet(url);
        for (String cookie : cookies) {
            method.addHeader("Cookie", cookie);
        }
        
        HttpResponse response = client.execute(method);
        HttpEntity entity = response.getEntity();
        if (response.getStatusLine().getStatusCode() < 400) {
            content = EntityUtils.toByteArray(entity);
            Header[] headers = response.getHeaders("Set-Cookie");
            for (Header h : headers) {
                cookies.add(h.getValue());
            }
        } else {
            EntityUtils.consume(entity);
        }
        client.close();
        return content;
    }

    public String getResponseAsString(String url, HashMap<String, String> map) throws IOException {
        String ret = "";
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpPost method = new HttpPost(url);
        for (String cookie : cookies) {
            method.addHeader("Cookie", cookie);
        }

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
    
    public String getResponseAsString(String url, HashMap<String, String> map, HashMap<String, String> headers) throws IOException {
        String ret = "";
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpPost method = new HttpPost(url);
        for (String cookie : cookies) {
            method.addHeader("Cookie", cookie);
        }

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
    
    public boolean downloadFile(String url, File output) throws IOException {
        boolean ret = false;
        
        FileUtil.sureDirExists(output, true);
        
        CloseableHttpClient client = WebClient.newHttpClient();
        HttpGet method = new HttpGet(url);
        for (String cookie : cookies) {
            method.addHeader("Cookie", cookie);
        }
        
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
