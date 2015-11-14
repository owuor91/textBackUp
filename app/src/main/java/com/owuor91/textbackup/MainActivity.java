package com.owuor91.textbackup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.MetadataChangeSet;


public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener{
    public String body, address, row, content="";
    public long datetime=0;
    ListView lvSMS;
    public Context context;
    SwipeRefreshLayout swipe;

    private static final String TAG = "Save file ";
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this.getBaseContext();
        setUpToolbar();
        getSMS();
        getEmail();
        saveToFile();
        swipeLayout();


    }


    private void saveFileToDrive(){
        Log.i(TAG, "Creating new contents.");

        Drive.DriveApi.newDriveContents(googleApiClient)
                .setResultCallback(new ResultCallback<DriveContentsResult>() {
                    @Override
                    public void onResult(DriveContentsResult driveContentsResult) {
                        if (!driveContentsResult.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents");
                            return;
                        }

                        Log.i(TAG, "New contents created");
                        OutputStream outputStream = driveContentsResult.getDriveContents().getOutputStream();

                        try {
                            FileInputStream fileInputStream = getBaseContext().openFileInput("sms_file.txt");
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            int nRead;
                            byte[] data = new byte[16384];
                            while ((nRead = fileInputStream.read(data, 0, data.length)) != -1) {
                                byteArrayOutputStream.write(data, 0, nRead);
                            }
                            byteArrayOutputStream.flush();
                            outputStream.write(byteArrayOutputStream.toByteArray());
                        } catch (IOException io) {
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

    public void swipeLayout(){
        swipe = (SwipeRefreshLayout)findViewById(R.id.swipelayout);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSMS();
                swipe.setRefreshing(false);
            }
        });
        swipe.setColorSchemeResources(R.color.red, R.color.amber, R.color.green);
    }


    public List<SMSData> getSMS(){
        List<SMSData> smsList = new ArrayList<SMSData>();
        lvSMS = (ListView)findViewById(R.id.lvSMS);

        Uri uri = Uri.parse("content://sms");
        Cursor c = getContentResolver().query(uri, null, null, null, null);

        if(c.moveToFirst()){
            for (int i = 0; i< c.getCount(); i++){
                SMSData sms = new SMSData();
                sms.setBody(c.getString(c.getColumnIndexOrThrow("body")).toString());
                sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")).toString());
                sms.setDate(c.getLong(c.getColumnIndexOrThrow("date")));
                smsList.add(sms);
                c.moveToNext();
            }
        }
        c.close();

        ListAdapter smsAdapter = new ListAdapter(this, smsList);

        lvSMS.setAdapter(smsAdapter);

        lvSMS.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SMSData contact = (SMSData) lvSMS.getItemAtPosition(position);

            }
        });

        return smsList;
    }


    public void saveToFile(){
        String fileName = "sms_file.txt";
        FileOutputStream outputStream;
           List<SMSData> messages = this.getSMS();
           for (int i=0; i<messages.size(); i++){
               address = messages.get(i).getNumber();
               body = messages.get(i).getBody();
               datetime = messages.get(i).getDate();
               row = address + " "+ body + " "+datetime;
               content+=row + "\n";
           }
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(content.getBytes());
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public  void getEmail(){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(context).getAccounts();
        Account account= accounts[0];
            if (emailPattern.matcher(account.name).matches()){
                String yourEmail = account.name;
                Toast.makeText(this, yourEmail, Toast.LENGTH_LONG).show();
            }
    }


    public void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Messages");
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
            Log.e(TAG, "Exception while starting resolution activity", e);
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
