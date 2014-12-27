package com.bengalbot.lightcast.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.bengalbot.lightcast.AppToolkit;
import com.bengalbot.lightcast.Constants;
import com.bengalbot.lightcast.R;
import com.bengalbot.lightcast.events.BluetoothDeviceConnectEvent;
import com.bengalbot.lightcast.events.SendCommandEvent;
import com.bengalbot.lightcast.view.AnimationView;
import com.bengalbot.lightcast.view.BrightnessView;
import com.bengalbot.lightcast.view.ColorView;
import com.bengalbot.lightcast.view.CommandView;
import com.bengalbot.lightcast.view.DelayView;
import com.bengalbot.lightcast.view.PresetViewActions;
import com.bengalbot.lightcast.view.StartView;
import com.bengalbot.lightcast.view.StopView;

import java.util.ArrayList;

/**
 */
public class PresetsFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, View.OnClickListener, PresetViewActions {

    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;

    private Handler mHandler;

    private Spinner mSpinner;
    private Button mSendButton;
    private ViewGroup mPresetHolder;
    private View mEmptyView;
    private boolean mScanning;

    public static PresetsFragment newInstance() {
        return new PresetsFragment();
    }

    @Override
    public String getFragmentTag() {
        return "PRESETS_FRAGMENT";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.presets_fragment, container, false);
        mHandler = new Handler();
        mSpinner = (Spinner)view.findViewById(R.id.devices_spinner);
        mPresetHolder = (ViewGroup)view.findViewById(R.id.preset_holder);
        mEmptyView = view.findViewById(R.id.empty_message);

        mSendButton = (Button)view.findViewById(R.id.send_commands);
        mSendButton.setOnClickListener(this);

        mSpinner.setOnItemSelectedListener(this);
        mLeDeviceListAdapter = new LeDeviceListAdapter(getActivity(), android.R.layout.simple_spinner_item, new ArrayList<BluetoothDevice>(0));

        setupPresetButtons(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }

        updateEmptyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mSpinner.setAdapter(mLeDeviceListAdapter);

        scanLeDevice(true);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 10000);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        final BluetoothDevice device = (BluetoothDevice)mLeDeviceListAdapter.getItem(position);
        if (device == null) return;

        //Post we have a device to connect
        AppToolkit.INSTANCE.post(new BluetoothDeviceConnectEvent(device));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void setupPresetButtons(View view) {

        view.findViewById(R.id.ic_brightness_btn).setOnClickListener(this);
        view.findViewById(R.id.ic_play_btn).setOnClickListener(this);
        view.findViewById(R.id.ic_stop_btn).setOnClickListener(this);
        view.findViewById(R.id.ic_delay_btn).setOnClickListener(this);
        view.findViewById(R.id.ic_color_btn).setOnClickListener(this);
        view.findViewById(R.id.ic_transition_btn).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        View viewToAdd = null;

        int id = view.getId();
        if (id == R.id.ic_brightness_btn) {
            viewToAdd = new BrightnessView(getActivity(), this);

        } else if (id == R.id.ic_play_btn) {
            viewToAdd = new StartView(getActivity(), this);
        } else if (id == R.id.ic_stop_btn) {
            viewToAdd = new StopView(getActivity(), this);
        } else if (id == R.id.ic_delay_btn) {
            viewToAdd = new DelayView(getActivity(), this);
        } else if (id == R.id.ic_color_btn) {
            viewToAdd = new ColorView(getActivity(), this);
        } else if (id == R.id.ic_transition_btn) {
            viewToAdd = new AnimationView(getActivity(), this);
        } else if (id == R.id.send_commands) {

            int childCount = mPresetHolder.getChildCount();
            for (int childIndex = 0; childIndex < childCount; childIndex++) {
                View v = mPresetHolder.getChildAt(childIndex);

                AppToolkit.INSTANCE.post(new SendCommandEvent(((CommandView)v).getCommand()));

                ;
            }
        }


        if (viewToAdd != null) {
            mPresetHolder.addView(viewToAdd);
        }
        updateEmptyView();
    }

    @Override
    public void onCloseView(View view) {
        mPresetHolder.removeView(view);
        updateEmptyView();
    }

    private void updateEmptyView() {
        mEmptyView.setVisibility(mPresetHolder.getChildCount() > 0 ? View.GONE : View.VISIBLE);

    }


}
