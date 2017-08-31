package com.haifeng.robot.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.haifeng.robot.R;
import com.haifeng.robot.view.Guanluocang;

/**
 * Created by dell on 2017/8/8.
 */

public class InfoActivity extends ActionBarActivity {

    private Guanluocang tvInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        (tvInfo) = (Guanluocang) findViewById(R.id.tvInfo);
        tvInfo.setText("4567890");
    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        IntentFilter usbFilter = new IntentFilter();
//        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        usbFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(mUsbReceiver, usbFilter);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        unregisterReceiver(mUsbReceiver);
//    }
//
//    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            tvInfo.append("BroadcastReceiver in\n");
//
//            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
//                tvInfo.append("ACTION_USB_DEVICE_ATTACHED\n");
//            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
//                tvInfo.append("ACTION_USB_DEVICE_DETACHED\n");
//            }
//        }
//    };
}