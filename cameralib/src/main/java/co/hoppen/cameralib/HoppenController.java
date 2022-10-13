package co.hoppen.cameralib;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import co.hoppen.cameralib.CallBack.ControllerFunction;
import co.hoppen.cameralib.CallBack.NotifyListener;
import co.hoppen.cameralib.CallBack.OnUsbStatusListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2022/9/28.
 */
public class HoppenController implements ControllerFunction, OnUsbStatusListener, NotifyListener {
   private CameraDevice cameraDevice = new CameraDevice();
   private McuDevice mcuDevice;

   public HoppenController(HoppenCamera.CameraConfig cameraConfig){
      cameraConfig.setNotifyListener(this);
      cameraDevice.setCameraConfig(cameraConfig);
      mcuDevice= new McuDevice(cameraConfig.getOnMoistureListener());
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

   @Override
   public void getMoisture() {
      if (cameraDevice.getDeviceConfig()!=null && !cameraDevice.getDeviceConfig().isMcuCommunication()){
         cameraDevice.sendInstruction(Instruction.MOISTURE);
      }else send(Instruction.MOISTURE);
   }

   @Override
   public void getProductCode() {
      send(Instruction.PRODUCT_CODE);
   }

   @Override
   public void getUniqueCode() {
      send(Instruction.UNIQUE_CODE);
   }

   @Override
   public void startPreview() {
      cameraDevice.startPreview();
   }

   @Override
   public void stopPreview() {
      cameraDevice.stopPreview();
   }

   @Override
   public void closeDevices() {
      cameraDevice.closeDevice();
      mcuDevice.closeDevice();
   }

   @Override
   public void capturePicture(CaptureResult captureResult) {
      capturePicture(0,0,captureResult);
   }

   @Override
   public void capturePicture(int width, int height,CaptureResult captureResult) {
      ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<Bitmap>() {
         @Override
         public Bitmap doInBackground() throws Throwable {
            Bitmap captureBitmap = null;
            if (cameraDevice.getCameraConfig()!=null && cameraDevice.getCameraConfig().getTextureView()!=null&&captureResult!=null){
               UVCCameraTextureView textureView = cameraDevice.getCameraConfig().getTextureView();
               if (width!=0 && height!=0){
                  captureBitmap = textureView.getBitmap(width,height);
               }else captureBitmap = textureView.getBitmap();
            }
            return captureBitmap;
         }

         @Override
         public void onSuccess(Bitmap result) {
            if (result!=null){
               captureResult.onCapture(result);
            }
         }
      });
   }

   private void send(Instruction instruction){
      cameraDevice.sendInstruction(instruction);
      mcuDevice.sendInstruction(instruction);
   }

   @Override
   public void onUpdateSurface(SurfaceTexture surfaceTexture) {
      cameraDevice.updateSurface(surfaceTexture);
   }

   @Override
   public void onPageStop() {
      stopPreview();
   }

   @Override
   public void onPageDestroy() {
      LogUtils.e("onPageDestroy");
      closeDevices();
   }

   public interface CaptureResult {
      void onCapture(Bitmap bitmap);
   }

}
