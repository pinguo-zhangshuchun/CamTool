package us.pinguo.camtool;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.mtp.MtpConstants;
import android.mtp.MtpDevice;
import android.mtp.MtpObjectInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.io.File;
import java.util.Collection;

import us.pinguo.camtool.utility.Logger;
import us.pinguo.camtool.utility.MessageDialog;


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
        initUSBMangr();
        registerUsbReceiver();
    }

    private void registerUsbReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mUsbReceiver = new USBReceiver();
        registerReceiver(mUsbReceiver, filter);
    }

    private void initUSBMangr() {
        mUsbMgr = (UsbManager) getSystemService(Context.USB_SERVICE);
    }

    public UsbDevice searchDevice() {
        UsbDevice device = null;
        Collection<UsbDevice> devices = mUsbMgr.getDeviceList().values();
        Logger.d(TAG, "devices:" + devices.size());
        for (UsbDevice dev : devices) {
            Logger.d(TAG, "found " + dev.getDeviceName());
            device = dev;
        }
        return device;
    }

    @Override
    protected void onResume() {
        super.onResume();
        UsbDevice device = searchDevice();
        if (null == device) {
            Logger.d(TAG, "No Camera Found");
            MessageDialog.info(this, "No Camera Found");
        } else {
            initDevice(device);
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

    class USBReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                MessageDialog.info(MainActivity.this, "Detect Camera has removed");
            }
        }
    }

    public void initDevice(UsbDevice device) {
        if (null != device) {
            talkWithCamera(device);
        }
    }

    private void talkWithCamera(UsbDevice device) {
        final MtpDevice mtpDevice = new MtpDevice(device);
        UsbDeviceConnection conn;
        if (mUsbMgr.hasPermission(device)) {
            conn = mUsbMgr.openDevice(device);
            int usbIntfCount = device.getInterfaceCount();
            Logger.d(TAG, "usb interface count:" + usbIntfCount);
            UsbInterface usbInterface = device.getInterface(0);
            conn.claimInterface(usbInterface, true);

        } else {
            Logger.d(TAG, "has no permission");
            Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
            intent.addCategory("android.hardware.usb.action.USB_DEVICE_DETACHED");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
            mUsbMgr.requestPermission(device, pi);
            return;
        }

        boolean ret = mtpDevice.open(conn);
        if (!ret) {
            Logger.d(TAG, "Failed open device");
            MessageDialog.info(this, "Failed open device");
            return;
        }

        int[] storageIDs = mtpDevice.getStorageIds();
        if (storageIDs == null || storageIDs.length < 1) {
            Logger.d(TAG, "invalid storageIds");
            MessageDialog.info(this, "Failed get storageIds");
            return;
        }
        Logger.d(TAG, "storageIDs:" + storageIDs.length);
        int storageId = storageIDs[0];
        final int[] objHandles = mtpDevice.getObjectHandles(storageId, 0, 0);
        Logger.d(TAG, "objHandles:" + objHandles.length);
        if (objHandles == null || objHandles.length < 1) {
            Logger.d(TAG, "invalid objHandles");
            return;
        }

        File dir = Environment.getExternalStorageDirectory();
        File path = new File(dir, "aaaaa");
        if (!path.exists()) {
            path.mkdir();
        }


        new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                int counter = 0;
                for (int handle : objHandles) {
                    if (copyMtpObject(mtpDevice, handle)) {
                        ++counter;
                    }
                }
                long finish = System.currentTimeMillis();
                Message msg = Message.obtain();
                msg.what = 200;
                msg.arg1 = counter;
                msg.arg2 = (int) (finish - start);
                mHandler.sendMessage(msg);
            }
        }.start();
    }


    private boolean copyMtpObject(MtpDevice device, int handle) {
        MtpObjectInfo info = device.getObjectInfo(handle);
        if (null == info) {
            Logger.d(TAG, "Failed get MtpObjectInfo");
            return false;
        }

        final String path = "aaaaa";
        int fmt = info.getFormat();
        if (fmt == MtpConstants.FORMAT_ASSOCIATION) {
            Logger.d(TAG, "directory:" + info.getName());
            return false;
        } else if (fmt == MtpConstants.FORMAT_EXIF_JPEG) {
            Logger.d(TAG, "exif jpeg:" + info.getName());
            return device.importFile(handle, path);
        } else if (fmt == MtpConstants.FORMAT_PNG) {
            Logger.d(TAG, "png:" + info.getName());
            return device.importFile(handle, path);
        } else if (fmt == MtpConstants.FORMAT_BMP) {
            Logger.d(TAG, "bmp:" + info.getName());
            return device.importFile(handle, path);
        } else {
            Logger.d(TAG, "Other format:" + info.getName());
            return false;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 200) {
                String text = "total:" + msg.arg1 + "\n" + "cost:" + msg.arg2;
                MessageDialog.info(MainActivity.this, text);
            }
        }
    };

}
