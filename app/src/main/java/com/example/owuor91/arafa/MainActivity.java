package com.example.owuor91.arafa;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.owuor91.textbackup.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{
    public String body, address, row, content="";
    public long datetime=0;
    ListView lvSMS;
    public Context context;
    SwipeRefreshLayout swipe;
    GoogleApiClient googleApiClient;
    private static final int RESOLVE_CONNECTION_REQUEST_CODE =2;

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

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()){
            try {
                connectionResult.startResolutionForResult(this,RESOLVE_CONNECTION_REQUEST_CODE);
            }
            catch (IntentSender.SendIntentException e){

            }
        }
        else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(),this,0).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode==RESULT_OK){
                    googleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(Bundle bundle) {

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
                Intent intent = new Intent(context,ViewText.class);
                startActivity(intent);
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
}
