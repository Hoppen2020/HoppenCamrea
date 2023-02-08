package co.hoppen.cameralib;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.hoppen.uvc.IButtonCallback;

import java.lang.ref.WeakReference;

import co.hoppen.cameralib.CallBack.NotifyListener;
import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.CallBack.OnInfoListener;
import co.hoppen.cameralib.CallBack.OnMoistureListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

import static com.hoppen.uvc.UVCCamera.FRAME_FORMAT_MJPEG;

/**
 * Created by YangJianHui on 2022/9/27.
 */
public class HoppenCamera{
   private UsbMonitor usbMonitor;

   private HoppenCamera(){
   }

   private HoppenController createController(CameraConfig cameraConfig){
      WeakReference<Context> contextWeakReference= new WeakReference<>(cameraConfig.textureView.getContext());
      Context context = contextWeakReference.get();
      if (context==null)return null;
      if (!(context instanceof LifecycleOwner)) return null;
      HoppenController controller = new HoppenController(cameraConfig);
      cameraConfig.textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
          @Override
          public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
              LogUtils.e(surface.toString());
              if (cameraConfig.surfaceTexture==null){
                  cameraConfig.surfaceTexture = surface;
              }else {
                  if (cameraConfig.getNotifyListener()!=null){
                      LogUtils.e(cameraConfig.surfaceTexture.toString(),surface.toString());
                      cameraConfig.surfaceTexture = surface;
                      cameraConfig.getNotifyListener().onUpdateSurface(surface);
                  }
              }
              if (usbMonitor==null) {
                  usbMonitor = new UsbMonitor(controller);
                  ((LifecycleOwner)context).getLifecycle().addObserver(new LifecycleEventObserver() {
                      @Override
                      public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                          if (usbMonitor==null)return;
                          if (event.equals(Lifecycle.Event.ON_CREATE)){
                              usbMonitor.connectListDevice(contextWeakReference.get());
                          }else if (event.equals(Lifecycle.Event.ON_START)){
                              usbMonitor.register(contextWeakReference.get());
                          }else if (event.equals(Lifecycle.Event.ON_RESUME)){
                             if (cameraConfig.getNotifyListener()!=null){
                                 cameraConfig.getNotifyListener().onPageResume();
                             }
                          } else if (event.equals(Lifecycle.Event.ON_STOP)){
                              usbMonitor.unregister(contextWeakReference.get());
                              if (cameraConfig.getNotifyListener()!=null){
                                  cameraConfig.getNotifyListener().onPageStop();
                              }
                          }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
                              if (cameraConfig.getNotifyListener()!=null){
                                  cameraConfig.getNotifyListener().onPageDestroy();
                              }
                          }
                      }
                  });
              }
          }

          @Override
          public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
          }
          @Override
          public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
              return false;
          }
          @Override
          public void onSurfaceTextureUpdated(SurfaceTexture surface) {
          }
      });
      return controller;
   }

   public static class Builder{
      private CameraConfig cameraConfig = null;

      public Builder(UVCCameraTextureView textureView){
          cameraConfig = new CameraConfig();
          cameraConfig.textureView = textureView;
      }

      public Builder setCameraButtonListener(IButtonCallback cameraButtonListener){
          cameraConfig.cameraButtonListener = cameraButtonListener;
          return this;
      }

      public Builder setResolution(int width ,int height) {
         cameraConfig.resolutionWidth = width;
         cameraConfig.resolutionHeight = height;
         return this;
      }

      public Builder setOnDeviceListener(OnDeviceListener onDeviceListener) {
         cameraConfig.onDeviceListener = onDeviceListener;
         return this;
      }

      public Builder setFrameFormat(int frameFormat) {
         cameraConfig.frameFormat = frameFormat;
         return this;
      }

      public Builder setOnMoistureListener(OnMoistureListener onMoistureListener) {
         cameraConfig.onMoistureListener = onMoistureListener;
         return this;
      }

      public Builder setOnInfoListener(OnInfoListener onInfoListener) {
         cameraConfig.onInfoListener = onInfoListener;
         return this;
      }

      public HoppenController build(){
         return new HoppenCamera().createController(cameraConfig);
      }

   }

   public static class CameraConfig{
       private SurfaceTexture surfaceTexture;
       private UVCCameraTextureView textureView;
       private int resolutionWidth;
       private int resolutionHeight;
       private OnDeviceListener onDeviceListener;
       private int frameFormat = FRAME_FORMAT_MJPEG;
       private OnMoistureListener onMoistureListener;
       private OnInfoListener onInfoListener;
       private IButtonCallback cameraButtonListener;
       private String devicePathName = "";
       private NotifyListener notifyListener;
       private boolean opened = false;

       public int getResolutionWidth() {
           return resolutionWidth;
       }

       public int getResolutionHeight() {
           return resolutionHeight;
       }

       public OnDeviceListener getOnDeviceListener() {
           return onDeviceListener;
       }

       public int getFrameFormat() {
           return frameFormat;
       }

       public OnMoistureListener getOnMoistureListener() {
           return onMoistureListener;
       }

       public OnInfoListener getOnInfoListener() {
           return onInfoListener;
       }

       public UVCCameraTextureView getTextureView() {
           return textureView;
       }

       public IButtonCallback getCameraButtonListener() {
           return cameraButtonListener;
       }

       public SurfaceTexture getSurfaceTexture() {
           return surfaceTexture;
       }

       public String getDevicePathName() {
           return devicePathName;
       }

       public void setDevicePathName(String devicePathName) {
           this.devicePathName = devicePathName;
       }

       public NotifyListener getNotifyListener() {
           return notifyListener;
       }

       public void setNotifyListener(NotifyListener notifyListener) {
           this.notifyListener = notifyListener;
       }

       public boolean isOpened() {
           return opened;
       }

       public void setOpened(boolean opened) {
           this.opened = opened;
       }
   }

}
