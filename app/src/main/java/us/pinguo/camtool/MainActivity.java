package us.pinguo.camtool;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


public class MainActivity extends ActionBarActivity {
    final static String TAG = "CamToolMainActivity";
    private final static String ACTION_USB_PERMISSION = "us.pinguo.camtool.USB_PERMISSION";
    private TextView mTextView;
    private USBReceiver mUsbReceiver;
    private UsbManager mUsbMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        Log.d(TAG, "onCreate");

        initUSB();
        logUSB();


        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        mUsbReceiver = new USBReceiver();
        registerReceiver(mUsbReceiver, filter);

    }

    private void initUSB() {
        mUsbMgr = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
        if (null != device) {
            talkWithCamera(device);
        } else {
            Log.d(TAG, "onResume device = null");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    private void initView() {
        mTextView = (TextView) findViewById(R.id.main_tv_content);
    }

    private void logUSB() {
        HashMap<String, UsbDevice> deviceHashMap = mUsbMgr.getDeviceList();
        StringBuilder sb = new StringBuilder();
        if (null != deviceHashMap) {
            int size = deviceHashMap.size();
            Log.d(TAG, "Detected " + size + " USB Cameras");
            if (size < 1) {
                Toast.makeText(this, "device number < 1", Toast.LENGTH_SHORT).show();
            } else {
                for (String key : deviceHashMap.keySet()) {
                    UsbDevice device = deviceHashMap.get(key);
                    Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                    intent.addCategory("android.hardware.usb.action.USB_DEVICE_DETACHED");
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
                    mUsbMgr.requestPermission(device, pi);
                    talkWithCamera(device);
//                    break; // Just process only one camera
                }
            }

        } else {
            sb.append("deviceHashMap = null\n");
        }

        mTextView.setText(sb.toString());
    }

    class USBReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action = " + action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    Log.d(TAG, "in receiver device=null ? " + (device == null));

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            //call method to set up device communication
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    }

    private void talkWithCamera(UsbDevice device) {
        MtpDevice mtpDevice = new MtpDevice(device);
        Log.d(TAG, "mtpDevice name:" + mtpDevice.getDeviceName());

        UsbDeviceConnection conn = null;
        if (mUsbMgr.hasPermission(device)) {
            conn = mUsbMgr.openDevice(device);
        } else {
            Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
            intent.addCategory("android.hardware.usb.action.USB_DEVICE_DETACHED");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
            mUsbMgr.requestPermission(device, pi);
        }

        boolean ret = mtpDevice.open(conn);
        if (!ret) {
            Log.d(TAG, "Failed open device");
        }

        int[] storageIDs = mtpDevice.getStorageIds();
        Log.d(TAG, "storageIDs:" + storageIDs.length);
        if (storageIDs == null || storageIDs.length < 1) {
            Log.d(TAG, "invalid storageIds");
        }

        int storageIds = storageIDs[0];

        int[] objHandles = mtpDevice.getObjectHandles(storageIds, 0, 0);
        Log.d(TAG, "objHandles:" + objHandles.length);
        if (objHandles == null || objHandles.length < 1) {
            Log.d(TAG, "invalid objHandles");
        }

        int objHandle = objHandles[0];
        MtpObjectInfo mtpObjectInfo = mtpDevice.getObjectInfo(objHandle);
        int depth, width, height;
        depth = mtpObjectInfo.getImagePixDepth();
        width = mtpObjectInfo.getImagePixWidth();
        height = mtpObjectInfo.getImagePixHeight();
        Log.d(TAG, "depth:" + depth + ",width:" + width + ",height:" + height);
    }

}
