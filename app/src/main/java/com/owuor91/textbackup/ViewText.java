package com.owuor91.textbackup;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveApi;
import com.owuor91.textbackup.R;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class ViewText extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener{

    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private GoogleApiClient googleApiClient;
    private Bitmap bitmapToSave;

    private void saveFileToDrive(){
        Log.i(TAG, "Creating new contents.");
        final Bitmap image = bitmapToSave;

        Drive.DriveApi.newDriveContents(googleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {
                    @Override
                    public void onResult(DriveContentsResult driveContentsResult) {
                        if (!driveContentsResult.getStatus().isSuccess()){
                            Log.i(TAG, "Failed to create new contents");
                            return;
                        }

                        Log.i(TAG,"New contents created");
                        OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.PNG, 100,bitmapStream);
                        try {
                            outputStream.write(bitmapStream.toByteArray());
                        }
                        catch (IOException io){
                            Log.i(TAG, "Unable to write file contents");
                        }

                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg")
                                .setTitle("Camera Photo.png")
                                .build();

                        IntentSender intentSender = Drive.DriveApi.newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(driveContentsResult.getDriveContents())
                                .build(googleApiClient);


                        try {
                            startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0,0,0);
                        }
                        catch (IntentSender.SendIntentException e){
                            Log.i(TAG,"Failed to launch file chooser");
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient==null){
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (googleApiClient != null){
            googleApiClient.disconnect();
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case REQUEST_CODE_CAPTURE_IMAGE:
                if (resultCode== Activity.RESULT_OK){
                    bitmapToSave = (Bitmap) data.getExtras().get("data");
                }
                break;
            case REQUEST_CODE_CREATOR:
                if (resultCode==RESULT_OK){
                    Log.i(TAG, "Image successfully saved");
                    bitmapToSave = null;
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),REQUEST_CODE_CAPTURE_IMAGE);
                }
                break;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection failed: "+ connectionResult.toString());
        if (!connectionResult.hasResolution()){
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(),0).show();
            return;
        }

        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        }
        catch (IntentSender.SendIntentException e){
            Log.e(TAG,"Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "API client connected");
        if (bitmapToSave==null){
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE),REQUEST_CODE_CAPTURE_IMAGE);
            return;
        }
        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleAPIClient connection suspended");
    }
}


/*

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_text);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


    }

public String goodtext = "Empty line";
FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
fab.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
        }
        });

        TextView textView= (TextView)findViewById(R.id.smsbody);


        try {
        FileInputStream fileInputStream = getBaseContext().openFileInput("sms_file.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line=bufferedReader.readLine())!=null){
        stringBuilder.append(line);
        goodtext+=line+"\n"+"\n";
        }
        }
        catch (Exception e){
        e.printStackTrace();
        }

        textView.setText(goodtext);*/
