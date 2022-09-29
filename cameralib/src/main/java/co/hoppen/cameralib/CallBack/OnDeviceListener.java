package co.hoppen.cameralib.CallBack;

import android.hardware.usb.UsbDevice;

import co.hoppen.cameralib.ErrorCode;

/**
 * Created by YangJianHui on 2021/3/15.
 */
public interface OnDeviceListener {

    void onConnected();

    void onDisconnect(ErrorCode errorCode);

}
