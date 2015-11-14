package com.owuor91.textbackup;


import android.content.IntentSender;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;


import java.io.ByteArrayOutputStream;

import java.io.FileInputStream;
import java.io.IOException;

import java.io.OutputStream;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class ViewText extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener{

    private static final String TAG = "Save file ";
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private GoogleApiClient googleApiClient;

    private void saveFileToDrive(){
        Log.i(TAG, "Creating new contents.");

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

                        try {
                            FileInputStream fileInputStream = getBaseContext().openFileInput("sms_file.txt");
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[16384];
                            while ((nRead=fileInputStream.read(data,0,data.length))!=-1){
                                byteArrayOutputStream.write(data,0,nRead);
                            }
                            byteArrayOutputStream.flush();
                            outputStream.write(byteArrayOutputStream.toByteArray());
                        }
                        catch (IOException io){
                            Log.i(TAG, "Unable to write file contents");
                        }

                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("text/plain")
                                .setTitle("txtfile")
                                .build();

                        Drive.DriveApi.getRootFolder(googleApiClient)
                                .createFile(googleApiClient, metadataChangeSet, driveContentsResult.getDriveContents());
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

        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleAPIClient connection suspended");
    }
}


