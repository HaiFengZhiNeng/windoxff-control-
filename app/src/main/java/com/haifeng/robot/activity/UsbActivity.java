package com.haifeng.robot.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.haifeng.robot.R;
import com.ocean.mvp.library.utils.L;

import java.util.HashMap;
import java.util.Iterator;

public class UsbActivity extends AppCompatActivity {

    private TextView tvInfo;
    private UsbManager mUsbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb);
        (tvInfo) = (TextView) findViewById(R.id.tvInfo);
        getUsbDecives();

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, usbFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            tvInfo.append("BroadcastReceiver in\n");

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                tvInfo.append("ACTION_USB_DEVICE_ATTACHED\n");
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                tvInfo.append("ACTION_USB_DEVICE_DETACHED\n");
            }
        }
    };

    public void getUsbDecives() {

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
        Iterator<UsbDevice> iterator = deviceHashMap.values().iterator();
        while (iterator.hasNext()) {
            UsbDevice device = iterator.next();
            tvInfo.setText("56789");
            tvInfo.append("\ndevice name: " + device.getDeviceName() + "\ndevice product name:"
                    + device.getProductName() + "\nvendor id:" + device.getVendorId() +
                    "\ndevice serial: " + device.getSerialNumber());
            L.e("key", device.getDeviceName());
        }
    }
}
