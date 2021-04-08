package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;

/**
 * Created by YangJianHui on 2021/3/16.
 */
public abstract class HoppenDevice implements OnUsbStatusListener{
    protected abstract boolean sendInstructions(Instruction instruction);
    protected abstract void setOnWaterListener(OnWaterListener onWaterListener);
    protected abstract void closeDevice();
}
