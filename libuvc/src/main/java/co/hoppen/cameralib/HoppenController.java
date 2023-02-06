package co.hoppen.cameralib;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import co.hoppen.cameralib.CallBack.CaptureCallback;
import co.hoppen.cameralib.CallBack.ControllerFunction;
import co.hoppen.cameralib.CallBack.NotifyListener;
import co.hoppen.cameralib.CallBack.OnUsbStatusListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2022/9/28.
 */
public class HoppenController implements ControllerFunction, OnUsbStatusListener{
   private final CameraDevice cameraDevice = new CameraDevice();
   private final McuDevice mcuDevice;

   public HoppenController(HoppenCamera.CameraConfig cameraConfig){
      cameraDevice.setCameraConfig(cameraConfig);
      mcuDevice= new McuDevice(cameraConfig);
      cameraConfig.setNotifyListener(new NotifyListener() {
         @Override
         public void onUpdateSurface(SurfaceTexture surfaceTexture) {
            cameraDevice.updateSurface(surfaceTexture);
         }

         @Override
         public void onPageResume() {
            LogUtils.e("onPageResume");
            startPreview();
            mcuDevice.startSystemOnline();
         }

         @Override
         public void onPageStop() {
            stopPreview();
            mcuDevice.stopSystemOnline();
         }

         @Override
         public void onPageDestroy() {
            LogUtils.e("onPageDestroy");
            closeDevices();
         }
      });
   }

   @Override
   public void onConnecting(UsbDevice usbDevice, DeviceType type) {
      switch (type){
         case CAMERA:
            cameraDevice.onConnecting(usbDevice);
            break;
         case MCU:
            mcuDevice.onConnecting(usbDevice);
            break;
      }
   }

   @Override
   public void onDisconnect(UsbDevice usbDevice, DeviceType type) {
      switch (type){
         case CAMERA:
            cameraDevice.onDisconnect(usbDevice);
            break;
         case MCU:
            mcuDevice.onDisconnect(usbDevice);
            break;
      }
   }

   @Override
   public void rgbLight() {
      send(Instruction.LIGHT_RGB);
   }

   @Override
   public void uvLight() {
      send(Instruction.LIGHT_UV);
   }

   @Override
   public void polarizedLight() {
      send(Instruction.LIGHT_POLARIZED);
   }

   @Override
   public void balancedPolarizedLight() {
      send(Instruction.LIGHT_BALANCED_POLARIZED);
   }

   @Override
   public void woodLight() {
      send(Instruction.LIGHT_WOOD);
   }

   @Override
   public void closeLight() {
      send(Instruction.LIGHT_CLOSE);
   }

   @Override
   public void sendInstruction(Instruction instruction) {
      send(instruction);
   }

   /**
    * 获取水分值
    */
   @Override
   public void getMoisture() {
      if (cameraDevice.getDeviceConfig()!=null && !cameraDevice.getDeviceConfig().isMcuCommunication()){
         cameraDevice.sendInstruction(Instruction.MOISTURE);
      }else send(Instruction.MOISTURE);
   }

   /**
    * 获取设备号
    */
   @Override
   public void getProductCode() {
      send(Instruction.PRODUCT_CODE);
   }

   /**
    * 获取设备唯一编码
    */
   @Override
   public void getUniqueCode() {
      send(Instruction.UNIQUE_CODE);
   }

   /**
    * 开始预览
    */
   @Override
   public void startPreview() {
      cameraDevice.startPreview();
   }

   /**
    * 停止预览
    */
   @Override
   public void stopPreview() {
      cameraDevice.stopPreview();
   }

   /**
    * 关闭设备
    */
   @Override
   public void closeDevices() {
      cameraDevice.closeDevice();
      mcuDevice.closeDevice();
   }

   /**
    * 流截图
    * @param captureCallback
    */
   @Override
   public void capturePicture(CaptureCallback captureCallback) {
      cameraDevice.captureImageInternal(0,0,captureCallback);
   }

   /**
    * 流截图
    * @param width
    * @param height
    * @param captureCallback
    */
   @Override
   public void capturePicture(int width, int height,CaptureCallback captureCallback) {
      cameraDevice.captureImageInternal(width,height,captureCallback);
   }


   /**
    * 控件截图
    * @param captureCallback
    */
   @Override
   public void captureViewPicture(CaptureCallback captureCallback) {
      cameraDevice.captureImageByViewInternal(0,0,captureCallback);
   }

   /**
    * 控件截图
    * @param width
    * @param height
    * @param captureCallback
    */
   @Override
   public void captureViewPicture(int width, int height, CaptureCallback captureCallback) {
      cameraDevice.captureImageByViewInternal(width,height,captureCallback);
   }

   /**
    * 发送指令
    * @param instruction
    */
   private void send(Instruction instruction){
      cameraDevice.sendInstruction(instruction);
      mcuDevice.sendInstruction(instruction);
   }
}
