package luckyclient.netty;

import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    /**
     * ʹ��HttpClient��JSON��ʽ����post����
     * @param urlParam
     * @param jsonparams
     * @param socketTimeout
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @author Seagull
     * @date 2019��5��14��
     */
    public static String httpClientPost(String urlParam,String jsonparams,Integer socketTimeout) throws NoSuchAlgorithmException, KeyManagementException, UnsupportedEncodingException, IOException{
        StringBuffer resultBuffer = null;
        CloseableHttpClient httpclient= HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(urlParam);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3000).setConnectionRequestTimeout(1500)
                .setSocketTimeout(socketTimeout).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-Type", "application/json");
        // �����������
        BufferedReader br = null;
        try {
            if(null!=jsonparams&&jsonparams.length()>0){
                StringEntity entity = new StringEntity(jsonparams,"utf-8");
                httpPost.setEntity(entity);
            }

            CloseableHttpResponse response = httpclient.execute(httpPost);

            // ��ȡ��������Ӧ����
            resultBuffer = new StringBuffer();
            if(null!=response.getEntity()){
                br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
                String temp;
                while ((temp = br.readLine()) != null) {
                    resultBuffer.append(temp);
                }
            }
        } catch (RuntimeException e) {
            log.error("�������ӳ����쳣������...",e);
            throw new RuntimeException(e);
        } catch (ConnectException e) {
            log.error("�ͻ��������޷����ӣ�����...",e);
            throw new ConnectException();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                    throw new RuntimeException(e);
                }
            }
        }
        return resultBuffer.toString();
    }

    /**
     * ʹ��HttpClient��JSON��ʽ����get����
     * @param urlParam
     * @param params
     * @param socketTimeout
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws NoHttpResponseException
     * @author Seagull
     * @date 2019��5��14��
     */
    public static String httpClientGet(String urlParam, Map<String, Object> params,Integer socketTimeout) throws NoSuchAlgorithmException, KeyManagementException, NoHttpResponseException {
        StringBuffer resultBuffer = null;
        CloseableHttpClient httpclient= HttpClients.createDefault();
        BufferedReader br = null;
        // �����������
        StringBuffer sbParams = new StringBuffer();
        if (params != null && params.size() > 0) {
            for (Entry<String, Object> entry : params.entrySet()) {
                sbParams.append(entry.getKey());
                sbParams.append("=");
                try {
                    sbParams.append(URLEncoder.encode(String.valueOf(entry.getValue()), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                sbParams.append("&");
            }
        }
        if (sbParams != null && sbParams.length() > 0) {
            urlParam = urlParam + "?" + sbParams.substring(0, sbParams.length() - 1);
        }
        HttpGet httpGet = new HttpGet(urlParam);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3000).setConnectionRequestTimeout(1500)
                .setSocketTimeout(socketTimeout).build();
        httpGet.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = httpclient.execute(httpGet);

            // ��ȡ��������Ӧ����
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
            String temp;
            resultBuffer = new StringBuffer();
            while ((temp = br.readLine()) != null) {
                resultBuffer.append(temp);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                    throw new RuntimeException(e);
                }
            }
        }
        return resultBuffer.toString();
    }


    /**
     * �ϴ��ļ�
     * @param urlParam
     * @param loadpath
     * @param file
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws HttpHostConnectException
     * @author Seagull
     * @date 2019��3��15��
     */
    public static String httpClientUploadFile(String urlParam, String loadpath, File file) throws NoSuchAlgorithmException, KeyManagementException, HttpHostConnectException {
        StringBuffer resultBuffer = null;
        CloseableHttpClient httpclient= HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(urlParam);
        // �����������
        BufferedReader br = null;
        try {
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            //��������ı����ʽ
            entityBuilder.setCharset(Charset.forName("utf-8"));
            entityBuilder.addBinaryBody("jarfile", file);
            entityBuilder.addTextBody("loadpath", loadpath);
            HttpEntity reqEntity =entityBuilder.build();
            httpPost.setEntity(reqEntity);

            CloseableHttpResponse response = httpclient.execute(httpPost);
            //��״̬���л�ȡ״̬��
            String responsecode = String.valueOf(response.getStatusLine().getStatusCode());
            // ��ȡ��������Ӧ����
            resultBuffer = new StringBuffer();

            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "utf-8"));
            String temp;
            while ((temp = br.readLine()) != null) {
                resultBuffer.append(temp);
            }
            if(resultBuffer.length()==0){
                resultBuffer.append("�ϴ��ļ��쳣����Ӧ�룺"+responsecode);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                    throw new RuntimeException(e);
                }
            }
        }
        return resultBuffer.toString();
    }

    /**
     * ��ȡ�ļ���
     * @param urlParam
     * @param params
     * @return
     * @throws IOException
     * @throws HttpHostConnectException
     * @author Seagull
     * @date 2019��3��15��
     */
    public static byte[] getFile(String urlParam, Map<String, Object> params) throws IOException, HttpHostConnectException {
        // �����������
        StringBuffer sbParams = new StringBuffer();
        if (params != null && params.size() > 0) {
            for (Entry<String, Object> entry : params.entrySet()) {
                sbParams.append(entry.getKey());
                sbParams.append("=");
                try {
                    sbParams.append(URLEncoder.encode(String.valueOf(entry.getValue()), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                sbParams.append("&");
            }
        }
        if (sbParams != null && sbParams.length() > 0) {
            urlParam = urlParam + "?" + sbParams.substring(0, sbParams.length() - 1);
        }
        URL urlConet = new URL(urlParam);
        HttpURLConnection con = (HttpURLConnection)urlConet.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(4 * 1000);
        InputStream inStream = con .getInputStream();    //ͨ����������ȡͼƬ����
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1 ){
            outStream.write(buffer, 0, len);
        }
        inStream.close();
        byte[] data =  outStream.toByteArray();
        return data;
    }

    /**
     * ������Url�������ļ�
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static boolean  downLoadFromUrl(String urlStr,String fileName,String savePath) throws IOException{
        try
        {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            //���ó�ʱ��Ϊ3��
            conn.setConnectTimeout(3*1000);
            //��ֹ���γ���ץȡ������403����
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            //�õ�������
            InputStream inputStream = conn.getInputStream();
            //��ȡ�Լ�����
            byte[] getData = readInputStream(inputStream);

            //�ļ�����λ��
            File saveDir = new File(savePath);
            if(!saveDir.exists()){
                saveDir.mkdir();
            }
            File file = new File(saveDir+File.separator+fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(getData);
            if(fos!=null){
                fos.close();
            }
            if(inputStream!=null){
                inputStream.close();
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ���������л�ȡ�ֽ�����
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


}