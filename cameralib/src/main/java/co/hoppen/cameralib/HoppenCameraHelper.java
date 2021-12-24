package co.hoppen.cameralib;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.view.TextureView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.LogUtils;

import java.util.List;

import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2021/3/15.
 */
public class HoppenCameraHelper implements LifecycleEventObserver,OnUsbStatusListener,TextureView.SurfaceTextureListener,OnErrorListener {
    private UsbMonitor usbMonitor;
    private HoppenController controller;
    private AppCompatActivity appCompatActivity;
    private boolean addObserver;
    private OnDeviceListener onDeviceListener;

//    private boolean stopTag = false;

    private HoppenCameraHelper(){

    }

    private HoppenController getController() {
        return controller;
    }

    private HoppenCameraHelper(AppCompatActivity activity, TextureView textureView){
        if (activity!=null){
            this.appCompatActivity = activity;
            UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
            textureView.setSurfaceTextureListener(this);
            usbMonitor = new UsbMonitor(usbManager,this);
            controller = new HoppenController(usbManager,this);
            if (this.onDeviceListener==null){
                if (activity instanceof OnDeviceListener) this.onDeviceListener = (OnDeviceListener) activity;
            }
        }
    }

    private HoppenCameraHelper(AppCompatActivity activity, TextureView textureView , OnDeviceListener onDeviceListener){
        this(activity,textureView);
        this.onDeviceListener = onDeviceListener;
    }

    public static HoppenController createController(AppCompatActivity activity,TextureView textureView){
        return new HoppenCameraHelper(activity,textureView).getController();
    }

    public static HoppenController createController(AppCompatActivity activity,TextureView textureView,OnDeviceListener onDeviceListener){
        return new HoppenCameraHelper(activity,textureView,onDeviceListener).getController();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        LogUtils.e(event);
        if (event.equals(Lifecycle.Event.ON_CREATE)){
            usbMonitor.requestDeviceList(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_START)){
            usbMonitor.register(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_STOP)){
//            stopTag = true;
            usbMonitor.unregister(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
            controller.close();
        }else if (event.equals(Lifecycle.Event.ON_RESUME)){
            new Handler().postDelayed(() -> controller.getCameraDevice().startPreview(),500);
        }
    }

    @Override
    public void onConnecting(UsbDevice usbDevice, DeviceType type) {
        if (controller!=null){
            if (type == DeviceType.MCU){
                controller.getMcuDevice().onConnecting(usbDevice,type);
            }else if (type == DeviceType.CAMERA){
                controller.getCameraDevice().onConnecting(usbDevice,type);
                if (onDeviceListener!=null)onDeviceListener.onConnected();
            }
        }
    }

    @Override
    public void onDisconnect(UsbDevice usbDevice, DeviceType type) {
        if (controller!=null){
            if (type == DeviceType.MCU){
                controller.getMcuDevice().onDisconnect(usbDevice,type);
            }else if (type == DeviceType.CAMERA){
                controller.getCameraDevice().onDisconnect(usbDevice,type);
                if (onDeviceListener!=null)onDeviceListener.onDisconnect(ErrorCode.NORMAL);
            }
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        controller.setSurfaceTexture(surface);
        if (!addObserver){
            appCompatActivity.getLifecycle().addObserver(HoppenCameraHelper.this);
            addObserver = true;
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

    @Override
    public void onError(ErrorCode errorCode) {
        if (onDeviceListener!=null)onDeviceListener.onDisconnect(ErrorCode.DEVICE_INFO_MISSING);
    }
}
