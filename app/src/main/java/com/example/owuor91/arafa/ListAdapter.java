package com.example.owuor91.arafa;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.owuor91.textbackup.R;

import java.util.List;

public class ListAdapter extends ArrayAdapter<SMSData> {
    private final Context context;
    private final List<SMSData> smsList;

    public ListAdapter(Context context, List<SMSData> smsList){
        super(context, R.layout.activity_main, smsList);
        this.context = context;
        this.smsList = smsList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView txtNumber = (TextView) rowView.findViewById(R.id.txtNumber);
        txtNumber.setText(smsList.get(position).getNumber());

        TextView txtBody = (TextView) rowView.findViewById(R.id.txtBody);
        txtBody.setText(smsList.get(position).getBody());
        return rowView;
    }
}
