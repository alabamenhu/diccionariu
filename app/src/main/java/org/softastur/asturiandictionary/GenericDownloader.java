package org.softastur.asturiandictionary;


import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GenericDownloader extends AsyncTask<String, Void, String> {

    private int mTag;
    private long mTime;
    private DownloaderCallback mCallback;
    private Map<String,String> post_param = new HashMap<String,String>();

    public interface DownloaderCallback {
        public void onRetrieveData(String result, int tag, long time);
    }

    public void addPostParameter(String key, String value) {
        post_param.put(key, value);
    }
    public void setCallback(DownloaderCallback callback) {
        mCallback = callback;
    }
    public void setTag(int tag) {mTag = tag;}
    public long getTime() {return mTime;}

    @Override
    protected String doInBackground(String... searchTerms) {
        URL url;
        HttpURLConnection urlConnection;
        String result;

        mTime = new Date().getTime();

        boolean needPostData = post_param.size() > 0;
        byte[] postDataBytes = new byte[]{};

        if(needPostData) {
            try {
                StringBuilder postData = new StringBuilder();
                for (Map.Entry<String, String> param : post_param.entrySet()) {
                    if (postData.length() != 0) postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
                }
                postDataBytes = postData.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                postDataBytes = new byte[]{};
            }
        }


        try {
            url = new URL(searchTerms[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            if(needPostData) {
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setRequestProperty("charset", "utf-8");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                urlConnection.setDoOutput(true);
                urlConnection.getOutputStream().write(postDataBytes);
            }
        }catch(Exception e) {
            return "";
        }

        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = inputStreamToString(in, "UTF-8");
        }catch(IOException e) {
            return "";
        }finally {
            urlConnection.disconnect();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result){
        mCallback.onRetrieveData(result,mTag,mTime);
    }

    private static final int BUFFER_SIZE = 4 * 1024;

    private String inputStreamToString(InputStream inputStream, String charsetName)
            throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(inputStream, charsetName);
        char[] buffer = new char[BUFFER_SIZE];
        int length;
        while ((length = reader.read(buffer)) != -1) {
            builder.append(buffer, 0, length);
        }
        return builder.toString();
    }

}
