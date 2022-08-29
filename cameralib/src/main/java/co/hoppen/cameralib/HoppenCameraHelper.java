package co.hoppen.cameralib;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;

import java.util.List;

import co.hoppen.cameralib.tools.queue.ConnectMcuDeviceTask;
import co.hoppen.cameralib.tools.queue.Task;
import co.hoppen.cameralib.tools.queue.TaskCallBack;
import co.hoppen.cameralib.tools.queue.TaskQueue;

/**
 * Created by YangJianHui on 2021/3/15.
 */
public class HoppenCameraHelper implements LifecycleEventObserver,OnUsbStatusListener,TextureView.SurfaceTextureListener,OnErrorListener {
    private UsbMonitor usbMonitor;
    private HoppenController controller;
    private AppCompatActivity appCompatActivity;
    private boolean addObserver;
    private OnDeviceListener onDeviceListener;
    private TaskQueue taskQueue;
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
            taskQueue = new TaskQueue();
        }
    }

    private HoppenCameraHelper(AppCompatActivity activity, TextureView textureView, OnDeviceListener onDeviceListener, List<DeviceFilter> list){
        if (activity!=null){
            this.appCompatActivity = activity;
            UsbManager usbManager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
            textureView.setSurfaceTextureListener(this);
            usbMonitor = new UsbMonitor(usbManager,this);
            if (list!=null && list.size()>0){
                usbMonitor.addDeviceFilter(true,list);
            }
            controller = new HoppenController(usbManager,this);
            if (this.onDeviceListener==null){
                if (activity instanceof OnDeviceListener) this.onDeviceListener = (OnDeviceListener) activity;
            }
            taskQueue = new TaskQueue();
        }
        this.onDeviceListener = onDeviceListener;
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

    public static HoppenController createController(AppCompatActivity activity,TextureView textureView,OnDeviceListener onDeviceListener,List<DeviceFilter>list){
        return new HoppenCameraHelper(activity,textureView,onDeviceListener,list).getController();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event.equals(Lifecycle.Event.ON_CREATE)){
            usbMonitor.requestDeviceList(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_START)){
            usbMonitor.register(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_STOP)){
            usbMonitor.unregister(appCompatActivity);
        }else if (event.equals(Lifecycle.Event.ON_DESTROY)){
            taskQueue.cancel();
            controller.close();
        }else if (event.equals(Lifecycle.Event.ON_RESUME)){
            new Handler().postDelayed(() -> controller.getCameraDevice().startPreview(),500);
        }
    }

    @Override
    public void onConnecting(UsbDevice usbDevice, DeviceType type) {
        if (controller != null){
            if (type == DeviceType.MCU){
                ConnectMcuDeviceTask connectMcuDeviceTask =
                        new ConnectMcuDeviceTask((UsbManager) appCompatActivity.getSystemService(Context.USB_SERVICE),usbDevice);
                taskQueue.addTask(connectMcuDeviceTask, new TaskQueue.currentTaskFinish() {
                    @Override
                    public void onFinish() {
                        ConnectMcuDeviceTask.ConnectMcuInfo connectMcuInfo = connectMcuDeviceTask.getConnectMcuInfo();
                        if (connectMcuInfo.isConform()){
                            controller.getMcuDevice().onConnecting(connectMcuInfo);
                        }
                    }
                });
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
        // 只添加一次
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
