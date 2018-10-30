package com.zopsmart.bluetoothmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.bluetooth.BluetoothAdapter.getDefaultAdapter;

public class BluetoothManager {

    private final DeviceType deviceType;
    private final Context context;
    private BroadcastReceiver broadcastReceiver;

    /**
     * Gives an object of BluetoothManager. This class is used search for a specified type of bluetooth device.
     *
     * @param context    Context in which the object is being initialized.
     * @param deviceType The type of bluetooth device that needs to be searched.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public BluetoothManager(Context context, DeviceType deviceType) {
        this.context = context;
        this.deviceType = deviceType;
    }

    /**
     * This method searches for the devices as specified by the filter. It sets up a broadcast receiver which will receive intents when a new BT device is found.
     *
     * @param deviceListener An implementation of deviceListener. Listens to the search event and gives back the appropriate search results
     */
    public void searchForDevices(final DeviceListener deviceListener) throws BluetoothException {
        if (deviceListener == null)
            throw new BluetoothException("DeviceListener cannot be null.");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                filterDevices(deviceListener);
            }
        };
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(broadcastReceiver, intentFilter);
        filterDevices(deviceListener);
    }

    /**
     * This method extracts the device list after broadcast listener is triggered and filters the results based on the selected device type.
     *
     * @param deviceListener An implementation of deviceListener. Listens to the search event and gives back the appropriate search results
     */
    @SuppressLint("MissingPermission")
    private void filterDevices(DeviceListener deviceListener) {
        try {
            Set<BluetoothDevice> bondedDevices = getDefaultAdapter().getBondedDevices();
            if (bondedDevices.size() == 0)
                return;
            List<Integer> btDeviceCodes = getDeviceCode();
            Set<BluetoothDevice> filteredDevices = new HashSet<>();
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                if (btDeviceCodes.contains(bluetoothDevice.getBluetoothClass().getMajorDeviceClass())) {
                    filteredDevices.add(bluetoothDevice);
                }
            }
            deviceListener.onDevicesFound(filteredDevices);
        } catch (Exception e) {
            deviceListener.onError(new BluetoothException(e));
        }
    }

    /**
     * When the activity that defined {@link BluetoothManager} is destroyed, this method should to be called.
     * This method unregisters the broadcast listener and prevents memory leak.
     */
    public void stopSearch() {
        if (broadcastReceiver != null)
            context.unregisterReceiver(broadcastReceiver);
    }

    /**
     * Reads the device type provided by the user and returns back the appropriate {@link BluetoothClass.Device.Major} of the specified device type to help with
     * filtering.
     *
     * @return integer returned if equal to the major bluetooth class code
     */
    public List<Integer> getDeviceCode() {
        List<Integer> deviceCodes = new ArrayList<>();
        switch (deviceType) {
            case PRINTER:
                deviceCodes.add(BluetoothClass.Device.Major.IMAGING);
                deviceCodes.add(BluetoothClass.Device.Major.PERIPHERAL);
                break;
            case COMPUTER:
                deviceCodes.add(BluetoothClass.Device.Major.COMPUTER);
                break;
            case AUDIO_VIDEO:
                deviceCodes.add(BluetoothClass.Device.Major.AUDIO_VIDEO);
                break;
        }
        return deviceCodes;
    }

    /**
     * This is the enum that is used to set the kind of device that needs to be found by {@link BluetoothManager}
     */
    public enum DeviceType {
        PRINTER, COMPUTER, AUDIO_VIDEO
    }

    /**
     * Interface that listens to newly found devices. Needs to implemented when calling {@link #searchForDevices(DeviceListener)}.
     * This callback provides the user with the list of found devices or error as needed.
     */
    public interface DeviceListener {
        void onDevicesFound(Set<BluetoothDevice> foundDevices);

        void onError(BluetoothException bluetoothException);
    }
}
