package co.hoppen.cameralib.CallBack;

import android.hardware.usb.UsbDevice;

import co.hoppen.cameralib.DeviceType;

/**
 * Created by YangJianHui on 2021/3/15.
 */
public interface OnUsbStatusListener {

    void onConnecting(UsbDevice usbDevice, DeviceType type);

    void onDisconnect(UsbDevice usbDevice,DeviceType type);

}
