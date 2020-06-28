package org.softastur.asturiandictionary;

import android.os.AsyncTask;
import android.os.Handler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by guifa on 9/5/17.
 */

public class RemoteFile {
    static final int TIMEOUT = 8000;

    RFDownloader downloader;
    RFTimer timer;
    OnReceiveDataListener callback;

    static public void accessFile(String url, OnReceiveDataListener callback, int tag, int timeout) {
        System.out.println("asturianu - accessFile() --> " + url);
        new RemoteFile(url,callback,tag,timeout,null);
    }

    static public void accessFileWithPost(String url, HashMap<String, String> parameters,
                                          OnReceiveDataListener callback, int tag, int timeout) {
        System.out.println("asturianu - accessFilePost() --> " + url);
        new RemoteFile(url,callback,tag,timeout,parameters);
    }

    private RemoteFile(String url, OnReceiveDataListener callback, int tag,
                       int timeout, HashMap<String,String> post) {
        this.callback = callback;
        downloader = new RFDownloader();
        downloader.setTag(tag);
        if(post != null) {
            for (Map.Entry<String, String> entry : post.entrySet()) {
                downloader.addPostParameter(
                        entry.getKey(),
                        entry.getValue()
                );
            }
        }
        downloader.execute(url);
        timer = new RFTimer(downloader,timeout < 1 ? TIMEOUT : timeout);
        (new Thread(timer)).start();
    }

    public interface OnReceiveDataListener {
        void onRetrieveData(String result, int tag, long time);
        void onTimeout(int tag, long time);

    }
    private void onRetrieveData(String result, int tag, long time) {
        if(callback != null) {
            callback.onRetrieveData(result, tag, time);
        }else{
            System.out.println("Unable to respond with remote data due to null callback object");
        }
    }
    private void onDataTimedOut(int tag, long time) {
        if(callback != null) {
            callback.onTimeout(tag,0);
        }else{
            System.out.println("Unable to respond with remote data due to null callback object");
        }
    }

    static class RFTimer implements Runnable {
        private int timeout = TIMEOUT;
        private RFDownloader downloader;
        private Handler handler = new Handler();

        @Override
        public void run() {
            handler.postDelayed(runnable, timeout);
            // After this period of time the task in run() of runnable will be done
        }

        public RFTimer(RFDownloader downloader,int timeout) {
            super();
            setDownloader(downloader);
            setTimeout(timeout);
        }

        private void setDownloader(RFDownloader downloader) {
            this.downloader = downloader;
        }
        private void setTimeout(int timeout) {
            this.timeout = timeout;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (downloader.getStatus() == AsyncTask.Status.RUNNING || downloader.getStatus() == AsyncTask.Status.PENDING) {
                    downloader.cancel(true); //Cancel Async task or do the operation you want after the delay
                }else{
                    Thread.currentThread().interrupt();
                }
            }
        };

    }

    class RFDownloader extends AsyncTask<String, Void, String> {

        private int mTag;
        private long mTime;
        private GenericDownloader.DownloaderCallback mCallback;
        private Map<String,String> post_param = new HashMap<String,String>();

        public void addPostParameter(String key, String value) {
            post_param.put(key, value);
        }
        public void setCallback(GenericDownloader.DownloaderCallback callback) {
            mCallback = callback;
        }
        public void setTag(int tag) {mTag = tag;}
        public long getTime() {return mTime;}

        @Override
        protected String doInBackground(String... searchTerms) {
            URL url;
            HttpURLConnection urlConnection;
            String result;
            System.out.println("asturianu - doInBackground(url) --> " + searchTerms[0]);

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
            System.out.println("asturianu - onPostExecute() -->\n" + result);
            onRetrieveData(result,mTag,mTime);
        }

        @Override
        protected void onCancelled() {
            onDataTimedOut(mTag,0); // TODO correct result
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
}
