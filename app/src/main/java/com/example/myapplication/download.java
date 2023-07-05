package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class download extends AsyncTask<String, Void, Void> {

    String url;
    static String fileName = "cat.jpg";
    static int responseCode;
    public download(String url){
        this.url = url;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try{
            URL download_url = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) download_url.openConnection();
            connection.connect();

            responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream is = connection.getInputStream();
                String folder = fileFolderDirectory();;
                File file = new File(folder + fileName);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len = -1;
                while((len = is.read(buf)) != -1){
                    fos.write(buf, 0, len);
                }
                fos.flush();
                fos.close();
            }
        }
        catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return null;
    }

    public String fileFolderDirectory() {
        String folder = Environment.getExternalStorageDirectory() + File.separator + "downloadFoloder" + File.separator;
        File directory = new File(folder);
        if(!directory.exists()){
            directory.mkdirs();
        }
        return folder;
    }

    public static String getRespondCode(){
        return Integer.toString(responseCode);
    }

    public static String getFileName(){
        return fileName;
    }
}


