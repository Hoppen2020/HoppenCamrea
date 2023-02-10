package co.hoppen.cameralib;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;

import java.lang.ref.WeakReference;

import co.hoppen.cameralib.CallBack.CaptureCallback;
import co.hoppen.cameralib.CallBack.ControllerFunction;
import co.hoppen.cameralib.CallBack.OnUsbStatusListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2022/9/28.
 */
public class HoppenController implements ControllerFunction, OnUsbStatusListener, TextureView.SurfaceTextureListener,LifecycleEventObserver {
   private final CameraDevice cameraDevice = new CameraDevice();
   private McuDevice mcuDevice;

   private UsbMonitor usbMonitor;

   private HoppenCamera.CameraConfig cameraConfig;

   private WeakReference<Context> contextWeakReference;

   public HoppenController(HoppenCamera.CameraConfig cameraConfig){
      this.cameraConfig = cameraConfig;
      cameraDevice.setCameraConfig(cameraConfig);
      mcuDevice= new McuDevice(cameraConfig);
      try {
         UVCCameraTextureView textureView = cameraConfig.getTextureView();
         contextWeakReference = new WeakReference<>(textureView.getContext());
         textureView.setSurfaceTextureListener(this);
      }catch (Exception e){
         LogUtils.e(e.toString());
      }
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

   @Override
   public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
      if (event.equals(Lifecycle.Event.ON_CREATE)){
         usbMonitor.connectListDevice(contextWeakReference.get());
      }else if (event.equals(Lifecycle.Event.ON_START)){
         usbMonitor.register(contextWeakReference.get());
      }else if (event.equals(Lifecycle.Event.ON_RESUME)){
         startPreview();
      } else if (event.equals(Lifecycle.Event.ON_STOP)){
         LogUtils.e(contextWeakReference.get()!=null);
         usbMonitor.unregister(contextWeakReference.get());
         stopPreview();
      }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
         closeDevices();
      }
   }


   //------------------SurfaceTextureListener---------------------
   @Override
   public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
              if (cameraConfig.getSurfaceTexture()==null){
                  cameraConfig.setSurfaceTexture(surfaceTexture);
              }else {
                 cameraConfig.setSurfaceTexture(surfaceTexture);
                 cameraDevice.updateSurface(surfaceTexture);
              }
              if (usbMonitor==null){
                 usbMonitor = new UsbMonitor(this);
                 if (contextWeakReference!=null&&contextWeakReference.get()!=null){
                    ((LifecycleOwner)contextWeakReference.get())
                            .getLifecycle()
                            .addObserver(this);
                 }
              }
   }

   @Override
   public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

   }

   @Override
   public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
      cameraDevice.setSurfaceDestroyed();
      return false;
   }

   @Override
   public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

   }


}
