package co.hoppen.cameralib;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Environment;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;
import com.hoppen.uvc.IButtonCallback;

import java.lang.ref.WeakReference;

import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.CallBack.OnFrameListener;
import co.hoppen.cameralib.CallBack.OnInfoListener;
import co.hoppen.cameralib.CallBack.OnMoistureListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

import static com.hoppen.uvc.UVCCamera.FRAME_FORMAT_MJPEG;
import static com.hoppen.uvc.UVCCamera.PIXEL_FORMAT_YUV420SP;

/**
 * Created by YangJianHui on 2022/9/27.
 */
public class HoppenCamera{
   private HoppenCamera(){
   }

   private HoppenController createController(CameraConfig cameraConfig){
       UVCCameraTextureView textureView = cameraConfig.getTextureView();
       if (textureView==null||!(textureView.getContext() instanceof LifecycleOwner))return null;
       return new HoppenController(cameraConfig);

//      WeakReference<Context> contextWeakReference= new WeakReference<>(cameraConfig.textureView.getContext());
//      Context context = contextWeakReference.get();
//      if (context==null)return null;
//      if (!(context instanceof LifecycleOwner)) return null;
//      HoppenController controller = new HoppenController(cameraConfig);
//      cameraConfig.textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//          @Override
//          public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//              LogUtils.e(surface.toString());
//              if (cameraConfig.surfaceTexture==null){
//                  cameraConfig.surfaceTexture = surface;
//              }else {
//                  if (cameraConfig.getNotifyListener()!=null){
//                      LogUtils.e(cameraConfig.surfaceTexture.toString(),surface.toString());
//                      cameraConfig.surfaceTexture = surface;
//                      cameraConfig.getNotifyListener().onUpdateSurface(surface);
//                  }
//              }
//              if (usbMonitor==null) {
//                  usbMonitor = new UsbMonitor(controller);
//                  ((LifecycleOwner)context).getLifecycle().addObserver(new LifecycleEventObserver() {
//                      @Override
//                      public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
//                          if (usbMonitor==null)return;
//                          if (event.equals(Lifecycle.Event.ON_CREATE)){
//                              usbMonitor.connectListDevice(contextWeakReference.get());
//                          }else if (event.equals(Lifecycle.Event.ON_START)){
//                              usbMonitor.register(contextWeakReference.get());
//                          }else if (event.equals(Lifecycle.Event.ON_RESUME)){
//                             if (cameraConfig.getNotifyListener()!=null){
//                                 cameraConfig.getNotifyListener().onPageResume();
//                             }
//                          } else if (event.equals(Lifecycle.Event.ON_STOP)){
//                              usbMonitor.unregister(contextWeakReference.get());
//                              if (cameraConfig.getNotifyListener()!=null){
//                                  cameraConfig.getNotifyListener().onPageStop();
//                              }
//                          }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
//                              if (cameraConfig.getNotifyListener()!=null){
//                                  cameraConfig.getNotifyListener().onPageDestroy();
//                              }
//                          }
//                      }
//                  });
//              }
//          }
//
//          @Override
//          public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            LogUtils.e("onSurfaceTextureSizeChanged");
//          }
//          @Override
//          public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//              LogUtils.e("onSurfaceTextureDestroyed");
//              if (cameraConfig.getNotifyListener()!=null){
//                  cameraConfig.getNotifyListener().onSurfaceDestroyed();
//              }
//              return false;
//          }
//          @Override
//          public void onSurfaceTextureUpdated(SurfaceTexture surface) {
////              LogUtils.e("onSurfaceTextureUpdated");
//          }
//      });
//      return controller;
   }

   public static class Builder{
      private CameraConfig cameraConfig = null;

      public Builder(UVCCameraTextureView textureView){
          cameraConfig = new CameraConfig();
          cameraConfig.textureView = new WeakReference<>(textureView);
      }

      public Builder setCameraButtonListener(IButtonCallback cameraButtonListener){
          cameraConfig.cameraButtonListener = new WeakReference<>(cameraButtonListener);
          return this;
      }

       public Builder setOnFrameListener(OnFrameListener onFrameListener){
           cameraConfig.onFrameListener = new WeakReference<>(onFrameListener);
           return this;
       }

      public Builder setResolution(int width ,int height) {
         cameraConfig.resolutionWidth = width;
         cameraConfig.resolutionHeight = height;
         return this;
      }

      public Builder setOnDeviceListener(OnDeviceListener onDeviceListener) {
         cameraConfig.onDeviceListener = new WeakReference<>(onDeviceListener);
         return this;
      }

      public Builder setFrameFormat(int frameFormat) {
         cameraConfig.frameFormat = frameFormat;
         return this;
      }

       public Builder setPixelFormat(int pixelFormat) {
           cameraConfig.pixelFormat = pixelFormat;
           return this;
       }

      public Builder setOnMoistureListener(OnMoistureListener onMoistureListener) {
         cameraConfig.onMoistureListener = new WeakReference<>(onMoistureListener);
         return this;
      }

      public Builder setOnInfoListener(OnInfoListener onInfoListener) {
         cameraConfig.onInfoListener = new WeakReference<>(onInfoListener);
         return this;
      }

      public Builder setCameraFilter(CameraFilter cameraFilter){
          cameraConfig.cameraFilter = cameraFilter;
          return this;
      }

      public Builder setFaceLightCount(FaceLightCount faceLightCount){
          cameraConfig.faceLightCount = faceLightCount;
          return this;
      }

//      public Builder setDefaultPreview(boolean preview){
//          cameraConfig.defaultPreview = preview;
//          return this;
//      }

      public HoppenController build(){
          //----------------test---------------------
            //LogUtils.getConfig().setDir(Environment.getExternalStorageDirectory().getPath()).setLog2FileSwitch(true);
          //----------------test---------------------
         LogUtils.e("Controller Built");
          return new HoppenCamera().createController(cameraConfig);
      }

   }

   public static class CameraConfig{
       private SurfaceTexture surfaceTexture;
       private int resolutionWidth;
       private int resolutionHeight;
       private int frameFormat = FRAME_FORMAT_MJPEG;
       private int pixelFormat = PIXEL_FORMAT_YUV420SP;
       private String devicePathName = "";
       private WeakReference<UVCCameraTextureView> textureView;
       private WeakReference<OnDeviceListener> onDeviceListener;
       private WeakReference<OnMoistureListener> onMoistureListener;
       private WeakReference<OnInfoListener> onInfoListener;
       private WeakReference<IButtonCallback> cameraButtonListener;
       private WeakReference<OnFrameListener> onFrameListener;
       private boolean opened = false;
       private FaceLightCount faceLightCount = FaceLightCount.FIVE;
//       private boolean defaultPreview = true;

       private CameraFilter cameraFilter = CameraFilter.NORMAL;

       public int getResolutionWidth() {
           return resolutionWidth;
       }

       public int getResolutionHeight() {
           return resolutionHeight;
       }

       public OnDeviceListener getOnDeviceListener() {
           return onDeviceListener!=null?onDeviceListener.get():null;
       }

       public int getFrameFormat() {
           return frameFormat;
       }

       public int getPixelFormat() {
           return pixelFormat;
       }

       public OnMoistureListener getOnMoistureListener() {
           return onMoistureListener!=null?onMoistureListener.get():null;
       }

       public OnInfoListener getOnInfoListener() {
           return onInfoListener!=null?onInfoListener.get():null;
       }

       public UVCCameraTextureView getTextureView() {
           return textureView!=null?textureView.get():null;
       }

       public IButtonCallback getCameraButtonListener() {
           return cameraButtonListener!=null?cameraButtonListener.get():null;
       }

       public OnFrameListener getOnFrameListener() {
           return onFrameListener!=null?onFrameListener.get():null;
       }

       public SurfaceTexture getSurfaceTexture() {
           return surfaceTexture;
       }

       public String getDevicePathName() {
           return devicePathName;
       }

       public CameraFilter getCameraFilter() {
           return cameraFilter;
       }

       public void setDevicePathName(String devicePathName) {
           this.devicePathName = devicePathName;
       }

       public boolean isOpened() {
           return opened;
       }

       public void setOpened(boolean opened) {
           this.opened = opened;
       }

       public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
           this.surfaceTexture = surfaceTexture;
       }

       public void setCameraFilter(CameraFilter cameraFilter) {
           this.cameraFilter = cameraFilter;
       }

       public FaceLightCount getFaceLightCount() {
           return faceLightCount;
       }

//       public boolean isDefaultPreview() {
//           return defaultPreview;
//       }

       public void clear(){
           if (textureView!=null)textureView.clear();
           if (onDeviceListener!=null)onDeviceListener.clear();
           if (onMoistureListener!=null)onMoistureListener.clear();
           if (onInfoListener!=null)onInfoListener.clear();
           if (cameraButtonListener!=null)cameraButtonListener.clear();
           if (onFrameListener!=null)onFrameListener.clear();
       }

   }

   public enum FaceLightCount{
       FIVE,
       THREE
   }

}
