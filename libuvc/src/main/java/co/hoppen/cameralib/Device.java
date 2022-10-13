package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;

/**
 * Created by YangJianHui on 2022/10/8.
 */
public abstract class Device {
   abstract void sendInstruction(Instruction instruction);
   abstract void onConnecting(UsbDevice usbDevice);
   abstract void onDisconnect(UsbDevice usbDevice);
   abstract void closeDevice();

}
