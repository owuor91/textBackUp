package com.example.owuor91.arafa;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String msgData;
    ListView lvSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<SMSData> smsList = new ArrayList<SMSData>();
        lvSMS = (ListView)findViewById(R.id.lvSMS);

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor c = getContentResolver().query(uri, null, null, null,null);

        if(c.moveToFirst()){
            for (int i = 0; i< c.getCount(); i++){
                SMSData sms = new SMSData();
                sms.setBody(c.getString(c.getColumnIndexOrThrow("body")).toString());
                sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")).toString());
                smsList.add(sms);
                c.moveToNext();
            }
        }
        c.close();

        ListAdapter smsAdapter = new ListAdapter(this, smsList);

        lvSMS.setAdapter(smsAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUpToolbar() {
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        
    }
}
