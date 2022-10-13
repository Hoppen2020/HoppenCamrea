package co.hoppen.cameralib;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.text.TextUtils;

import com.blankj.utilcode.util.Utils;

/**
 * 描述：usb控制信息类
 * 作者：liugh
 * 日期：2018/11/21
 * 版本：v2.0.0
 */
public class ControlBlock {
    private static final String DEFAULT_USBFS = "/dev/bus/usb";
    private UsbDevice mUsbDevice;
    private UsbDeviceConnection mUsbConnection;

    public ControlBlock(UsbDevice usbDevice) {
        this.mUsbDevice = usbDevice;
    }

    public UsbDeviceConnection open() {
        if (mUsbDevice != null) {
            UsbManager usbManager = (UsbManager) Utils.getApp().getSystemService(Context.USB_SERVICE);
            mUsbConnection = usbManager.openDevice(mUsbDevice);
        }
        return mUsbConnection;
    }

    public void close() {
        if (mUsbConnection != null) {
            mUsbConnection.close();
            mUsbConnection = null;
        }
    }

    public UsbDeviceConnection getConnection() {
        return mUsbConnection;
    }

    public UsbDevice getUsbDevice() {
        return mUsbDevice;
    }

    public int getVendorId() {
        return mUsbDevice != null ? mUsbDevice.getVendorId() : 0;
    }

    public int getProductId() {
        return mUsbDevice != null ? mUsbDevice.getProductId() : 0;
    }

    public int getFileDescriptor() {
        return mUsbConnection != null ? mUsbConnection.getFileDescriptor() : 0;
    }

    public int getBusNum() {
        int busnum = 0;
        if (mUsbDevice != null) {
            String name = mUsbDevice.getDeviceName();
            String[] info = !TextUtils.isEmpty(name) ? name.split("/") : null;
            if (info != null && info.length > 1) {
                busnum = Integer.parseInt(info[info.length - 2]);
            }
        }
        return busnum;
    }

    public int getDevNum() {
        int devnum = 0;
        if (mUsbDevice != null) {
            String name = mUsbDevice.getDeviceName();
            String[] info = !TextUtils.isEmpty(name) ? name.split("/") : null;
            if (info != null && info.length > 0) {
                devnum = Integer.parseInt(info[info.length - 1]);
            }
        }
        return devnum;
    }

    public String getUSBFSName() {
        String result = null;
        if (mUsbDevice != null) {
            final String name = mUsbDevice.getDeviceName();
            final String[] info = !TextUtils.isEmpty(name) ? name.split("/") : null;
            if (info != null && info.length > 2) {
                final StringBuilder sb = new StringBuilder(info[0]);
                for (int i = 1; i < info.length - 2; i++) {
                    sb.append("/").append(info[i]);
                }
                result = sb.toString();
            }
        }
        return !TextUtils.isEmpty(result) ? result : DEFAULT_USBFS;
    }

}
