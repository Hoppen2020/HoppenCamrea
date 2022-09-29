package co.hoppen.cameralib;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import co.hoppen.cameralib.CallBack.OnCaptureListener;
import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.CallBack.OnInfoListener;
import co.hoppen.cameralib.CallBack.OnWaterListener;

import static com.serenegiant.usb.UVCCamera.FRAME_FORMAT_MJPEG;

/**
 * Created by YangJianHui on 2022/9/27.
 */
public class HoppenCamera implements LifecycleEventObserver {
   private UsbMonitor usbMonitor;
   private Builder builder;

   private HoppenCamera(){
      usbMonitor = new UsbMonitor();
   }

   @Override
   public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
      if (event.equals(Lifecycle.Event.ON_CREATE)){
         usbMonitor.requestDeviceList(builder.context);
      }else if (event.equals(Lifecycle.Event.ON_START)){
         usbMonitor.register(builder.context);
      }else if (event.equals(Lifecycle.Event.ON_STOP)){
         usbMonitor.unregister(builder.context);
      }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
//         taskQueue.cancel();
//         controller.close();
      }else if (event.equals(Lifecycle.Event.ON_RESUME)){
         //new Handler().postDelayed(() -> controller.getCameraDevice().startPreview(),500);
      }
   }

   private HoppenController2 createController(Builder builder){
      this.builder = builder;
      Context context = builder.context;
      if (!(context instanceof LifecycleOwner)) return null;
      ((LifecycleOwner)context).getLifecycle().addObserver(this);
       HoppenController2 hoppenController2 = new HoppenController2();
       usbMonitor.setOnUsbStatusListener(hoppenController2);
      return hoppenController2;
   }


   public static class Builder{
      private Context context;//仅用来监听activity情况 禁止其他地方引用
      private int resolutionWidth = 640;
      private int resolutionHeight = 480;
      private String specifyDeviceName;
//      private UVCCameraTextureView textureView;
      private OnDeviceListener onDeviceListener;
      private int frameFormat = FRAME_FORMAT_MJPEG;
      private OnWaterListener onWaterListener;
      private OnInfoListener onInfoListener;
      private OnCaptureListener onCaptureListener;

      public Builder(Context context){
         this.context = context;
      }

      public Builder setResolution(int width ,int height) {
         this.resolutionWidth = width;
         this.resolutionHeight = height;
         return this;
      }

      public Builder setSpecifyDeviceName(String specifyDeviceName) {
         this.specifyDeviceName = specifyDeviceName;
         return this;
      }

//      public Builder setTextureView(UVCCameraTextureView textureView) {
//         this.textureView = textureView;
//         return this;
//      }

      public Builder setOnDeviceListener(OnDeviceListener onDeviceListener) {
         this.onDeviceListener = onDeviceListener;
         return this;
      }

      public Builder setFrameFormat(int frameFormat) {
         this.frameFormat = frameFormat;
         return this;
      }

      public Builder setOnWaterListener(OnWaterListener onWaterListener) {
         this.onWaterListener = onWaterListener;
         return this;
      }

      public Builder setOnInfoListener(OnInfoListener onInfoListener) {
         this.onInfoListener = onInfoListener;
         return this;
      }

      public Builder setOnCaptureListener(OnCaptureListener onCaptureListener) {
         this.onCaptureListener = onCaptureListener;
         return this;
      }

      public HoppenController2 build(){
         return new HoppenCamera().createController(this);
      }


   }

}
