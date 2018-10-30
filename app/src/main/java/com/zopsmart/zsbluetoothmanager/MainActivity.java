package com.zopsmart.zsbluetoothmanager;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zopsmart.bluetoothmanager.BluetoothException;
import com.zopsmart.bluetoothmanager.BluetoothManager;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    BluetoothManager bluetoothManager;
    TextView tvText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvText = findViewById(R.id.tv_text);
        bluetoothManager = new BluetoothManager(this, BluetoothManager.DeviceType.COMPUTER);
        String first = "Searching for " + bluetoothManager.getDeviceCode() + " devices...\n";
        tvText.setText(first);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    findDevices();
                } catch (BluetoothException e) {
                   e.printStackTrace();
                }
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    private void findDevices() throws BluetoothException {
        bluetoothManager.searchForDevices(new BluetoothManager.DeviceListener() {
            @Override
            public void onDevicesFound(Set<BluetoothDevice> foundDevices) {
                for (BluetoothDevice bluetoothDevice : foundDevices) {
                    addText(bluetoothDevice.getName() + " " + bluetoothDevice.getBluetoothClass().getMajorDeviceClass());
                }
            }

            @Override
            public void onError(BluetoothException bluetoothException) {

            }
        });
    }

    private void addText(String s) {
        String text = tvText.getText().toString() + " " + s;
        tvText.setText(text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothManager.stopSearch();
    }
}
