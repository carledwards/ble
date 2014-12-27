package com.bengalbot.lightcast.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.bengalbot.lightcast.R;

import java.util.List;

public class LeDeviceListAdapter extends ArrayAdapter implements SpinnerAdapter {

    private List<BluetoothDevice> arrayList;

    public LeDeviceListAdapter(Context context, int resource, List<BluetoothDevice> items) {
        super(context, resource, items);
        arrayList = items;

    }

    public void addDevice(BluetoothDevice device) {
        if (!arrayList.contains(device)) {
            arrayList.add(device);
        }
    }

    @Override
    public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {

        if (convertView == null)  {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.device_spinner_item, null);
        }

        BluetoothDevice device = arrayList.get(position);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.device_name);

        nameTextView.setText("Connected to: " + device.getName());

        TextView uuidTextView = (TextView) convertView.findViewById(R.id.device_uuid);
        uuidTextView.setText(device.getAddress());


        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent){
        if (convertView == null)  {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.device_spinner_item, null);
        }

        BluetoothDevice device = arrayList.get(position);

        TextView nameTextView = (TextView) convertView.findViewById(R.id.device_name);
        nameTextView.setText(device.getName());

        TextView uuidTextView = (TextView) convertView.findViewById(R.id.device_uuid);
        uuidTextView.setText(device.getAddress());

        return convertView;
    }
}