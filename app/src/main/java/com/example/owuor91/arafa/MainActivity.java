package com.example.owuor91.arafa;

import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    String msgData;
    TextView txtsms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtsms= (TextView)findViewById(R.id.txtsms);

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null,null,null);
        if(cursor.moveToFirst()){
            do {
                msgData = "";
                for (int idx=0; idx<cursor.getColumnCount(); idx++){
                    msgData += ""+cursor.getColumnName(idx)+":"+cursor.getString(idx);
                }

            }
            while (cursor.moveToNext());
        }
        else {
            Toast.makeText(this, "You have no messages", Toast.LENGTH_SHORT).show();
        }

        txtsms.setText(msgData);
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
}
