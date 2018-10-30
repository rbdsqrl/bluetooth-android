package com.zopsmart.bluetoothmanager;

/**
 * This class extends {@link Exception}. It handles all the exceptions related to Bluetooth manager.
 */
public class BluetoothException extends Exception {

    BluetoothException(Exception e) {
        super(e);
    }

    BluetoothException(String exception) {
        super(exception);
    }
}
