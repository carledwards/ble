package com.bengalbot.android.events;

import android.bluetooth.BluetoothDevice;

/**
 */
public class BluetoothDeviceConnectEvent {

    public final BluetoothDevice device;

    public BluetoothDeviceConnectEvent(BluetoothDevice device) {
        this.device = device;
    }

}
