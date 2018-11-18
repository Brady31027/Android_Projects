package com.ojsb.writetousb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;
import com.github.mjdev.libaums.fs.UsbFileOutputStream;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private String TAG = "MTKUSB";
    private static final int TOAST_USB_ATTACHED = 0;
    private static final int TOAST_USB_DETACHED = 1;
    private static final int TOAST_USB_PERMISSION_GRANTED = 2;
    private static final int TOAST_USB_PERMISSION_DENIED = 3;
    private static final String ACTION_USB_PERMISSION = "com.ojsb.writetousb.USB_PERMISSION";
    private static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    private static final boolean mReadFilesFromUSB = false;
    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    protected UsbMassStorageDevice mDev;

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "get intent action: "+ action);

            if (action.equalsIgnoreCase(ACTION_USB_ATTACHED)) {
                synchronized (this) {
                    showToast(TOAST_USB_ATTACHED);
                    checkUsb();
                }
            } else if (action.equalsIgnoreCase(ACTION_USB_DETACHED)) {
                synchronized (this) {
                    // TODO: do whatever you want if the USB is detached
                    showToast(TOAST_USB_DETACHED);
                }
            } else if (action.equalsIgnoreCase(ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        showToast(TOAST_USB_PERMISSION_GRANTED);
                        writeFileToUsb();
                    } else {
                        showToast(TOAST_USB_PERMISSION_DENIED);
                    }

                }
            }
        }
    };

    private void checkUsb() {
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<String> iter = deviceList.keySet().iterator();

        while (iter.hasNext()) {
            String deviceName = iter.next();
            UsbDevice device = deviceList.get(deviceName);
            String VID = Integer.toHexString(device.getVendorId()).toUpperCase();
            String PID = Integer.toHexString(device.getProductId()).toUpperCase();
            String DNAME = device.getDeviceName();
            String PNAME = device.getProductName();

            Log.d(TAG, "VID: "+ VID + ", PID: "+ PID +
                    ", Device Name: " + DNAME +
                    ", Product Name: " + PNAME);

            if (!mUsbManager.hasPermission(device)) {
                Log.d(TAG, "USB permission denied");

                // pop-up a dialog to let user grant the usb permission
                mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                mUsbManager.requestPermission(device, mPermissionIntent);

            } else {
                Log.d(TAG, "USB permission granted");
                writeFileToUsb();
            }

        }
    }

    private void showToast(int code) {
        switch (code) {
            case TOAST_USB_ATTACHED:
                Toast.makeText(this, "USB is attached", Toast.LENGTH_SHORT).show();
                break;
            case TOAST_USB_DETACHED:
                Toast.makeText(this, "USB is detached", Toast.LENGTH_SHORT).show();
                break;
            case TOAST_USB_PERMISSION_GRANTED:
                Toast.makeText(this, "USB permission is granted", Toast.LENGTH_SHORT).show();
                break;
            case TOAST_USB_PERMISSION_DENIED:
                Toast.makeText(this, "USB permission is denied", Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(this, "Undefined behavior", Toast.LENGTH_SHORT).show();
        }

    }

    private void writeFileToUsb() {
        UsbMassStorageDevice[] massStorageDevices = UsbMassStorageDevice.getMassStorageDevices(this);
        for (UsbMassStorageDevice dev: massStorageDevices) {

            try {
                dev.init();
                FileSystem fs = dev.getPartitions().get(0).getFileSystem();
                UsbFile root = fs.getRootDirectory();
                if (mReadFilesFromUSB == true) {
                    UsbFile[] files = root.listFiles();
                    for (UsbFile file: files) {
                        Log.d(TAG, file.getName());
                    }
                }

                // create a new folder in USB drive
                UsbFile newDir = root.createDirectory("testing_dir");

                // create a new file in <USB_ROOT>/<newDir>
                UsbFile newFile = newDir.createFile("testing_file");

                // write something to a file
                OutputStream os = new UsbFileOutputStream(newFile);
                os.write("Of course we can write to usb drive!".getBytes());
                os.close();

                dev.close();

            }catch(Exception e) {
                Log.e(TAG, "something went wrong while init dev");
                dev.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "registered broadcast receiver");

        // register broadcast receiver
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_ATTACHED);
        filter.addAction(ACTION_USB_DETACHED);

        registerReceiver(mUsbReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }
}
