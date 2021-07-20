package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;

/**
 * Created by YangJianHui on 2021/3/15.
 */
public interface OnDeviceListener {

    void onConnected();

    void onDisconnect(ErrorCode errorCode);

}
