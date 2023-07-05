package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    boolean storage_permissionGranted = false;
    final int REQUEST_STORAGE_PERMISSION = 101;
    AlertDialog.Builder permission_Dialog;
    Button button_downloadFile;
    Button button_uploadFile;
    TextView response_statusText;
    TextView upload_fileText;
    DownloadTrigger download_trigger = new DownloadTrigger();
    UploadTrigger upload_trigger = new UploadTrigger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getStoragePermission();

        componentInit();
        button_downloadFile.setOnClickListener(download_trigger);
        button_uploadFile.setOnClickListener(upload_trigger);
    }

    private void getStoragePermission(){
       if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
           //Toast.makeText(this,"已獲取權限",Toast.LENGTH_SHORT).show();
           storage_permissionGranted = true;
       }
       else{
           //Toast.makeText(this,"尚未獲取權限",Toast.LENGTH_SHORT).show();
           requestStoragePermissions();
       }
    }

    private void requestStoragePermissions() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            permission_Dialog = new AlertDialog.Builder(this);
            permission_Dialog
                    .setMessage("此應用程式，需要儲存空間權限才能正常使用")
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestStoragePermissions();
                        }
                    })
                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                        }
                    }).show();
        }
        else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case REQUEST_STORAGE_PERMISSION:
                if(grantResults.length > 0){
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        storage_permissionGranted = true;
                    }
                    else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                        if(!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                            Toast.makeText(this, "儲存空間權限已被關閉，功能將無法正常使用", Toast.LENGTH_SHORT).show();

                            permission_Dialog = new AlertDialog.Builder(this);
                            permission_Dialog
                                    .setTitle("開啟儲存空間權限")
                                    .setMessage("此應用程式，儲存空間權限已被關閉，需開啟才能正常使用")
                                    .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                           Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                           Uri uri = Uri.fromParts("package", getPackageName(), null);
                                           intent.setData(uri);
                                           startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            requestStoragePermissions();
                                        }
                                    }).show();
                        }
                        else{
                            Toast.makeText(this, "儲存空間權限已被關閉，功能將無法正常使用", Toast.LENGTH_SHORT).show();
                            requestStoragePermissions();
                        }
                    }
                }
                break;
        }
    }

    private void componentInit(){
        button_downloadFile = (Button) findViewById(R.id.button_download);
        button_uploadFile = (Button) findViewById(R.id.button_upload);
        response_statusText = (TextView) findViewById(R.id.response_status);
        upload_fileText = (TextView) findViewById(R.id.upload_file);
    }

    private class DownloadTrigger implements View.OnClickListener{
        @Override
        public void onClick(View v){
            new download("http://192.168.0.2/cat.jpg").execute();
            try {
                Thread.sleep(500);
                Toast.makeText(MainActivity.this, "Download Complete", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e){
                Log.d(TAG, e.getLocalizedMessage());
            }
            response_statusText.setText("Status: " + download.getRespondCode());
            upload_fileText.setText(download.getFileName());
        }
    }

    private class UploadTrigger implements  View.OnClickListener{
        @Override
        public void onClick(View v){
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            resultLauncher.launch(intent);
        }
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK && result.getData() != null){
                Uri photoUri = result.getData().getData();
                new upload(getRealPathFromURI(photoUri), "http://192.168.0.2/upload.php").execute();

                try {
                    Thread.sleep(500);
                    Toast.makeText(MainActivity.this, "Upload Complete", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Log.d(TAG, e.getLocalizedMessage());
                }
                response_statusText.setText("Status: " + upload.getRespondCode());
                upload_fileText.setText(upload.getFileName());
            }
        }
    });

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}