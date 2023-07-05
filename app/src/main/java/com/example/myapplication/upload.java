package com.example.myapplication;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class upload extends AsyncTask<String,Void,Void> {

    static String target_path;
    String target_url;
    static int responseCode;

    public upload(String path,String url){
        this.target_path = path;
        this.target_url = url;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try{
            File imageFile = new File(target_path);
            URL url = new URL(target_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String boundary = "====================";
            String lineBreak = "\r\n";

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            // 寫入檔案開始標示
            OutputStream os = connection.getOutputStream();
            String startingBoundary = "--" + boundary + lineBreak;
            os.write(startingBoundary.getBytes());

            // 寫入檔案內容
            String fileName = imageFile.getName();
            String imagePartHeader = "Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"" + fileName + "\"" + lineBreak;
            os.write(imagePartHeader.getBytes());

            FileInputStream fis = new FileInputStream(imageFile);
            byte[] buf = new byte[1024];
            int len = -1;
            while((len = fis.read(buf)) != -1){
                os.write(buf, 0, len);
            }
            fis.close();

            // 寫入檔案結束標示
            String endingBoundary = lineBreak + "--" + boundary + "--" + lineBreak;
            os.write(endingBoundary.getBytes());

            // 接收伺服器回應
            responseCode = connection.getResponseCode();
            System.out.println(responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                System.out.println("Server Response: " + response.toString());
            }
            else {
                System.out.println("Error: " + responseCode);
            }
            connection.disconnect();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRespondCode(){
        return Integer.toString(responseCode);
    }
    public static String getFileName(){
        return target_path;
    }
}
