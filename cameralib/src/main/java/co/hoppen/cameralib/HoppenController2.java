package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;

import co.hoppen.cameralib.CallBack.ControllerFunction;
import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.CallBack.OnUsbStatusListener;

/**
 * Created by YangJianHui on 2022/9/28.
 */
public class HoppenController2 implements ControllerFunction, OnUsbStatusListener {
   private CameraDevice cameraDevice;
   private McuDevice mcuDevice;
//   public void setCameraDevice(CameraDevice cameraDevice) {
//      if (this.cameraDevice==null){
//         this.cameraDevice = cameraDevice;
//      }
//   }
//
//   public void setMcuDevice(McuDevice mcuDevice) {
//      if (this.mcuDevice==null){
//         this.mcuDevice = mcuDevice;
//      }
//   }

   public HoppenController2(){

   }


   @Override
   public void onConnecting(UsbDevice usbDevice, DeviceType type) {

   }

   @Override
   public void onDisconnect(UsbDevice usbDevice, DeviceType type) {

   }
}
