package co.hoppen.cameralib;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbManager;
import android.view.Surface;

import com.blankj.utilcode.util.Utils;
import com.serenegiant.usb.Size;

import java.util.List;

import co.hoppen.cameralib.CallBack.Controller;
import co.hoppen.cameralib.CallBack.OnButtonListener;
import co.hoppen.cameralib.CallBack.OnErrorListener;
import co.hoppen.cameralib.CallBack.OnInfoListener;
import co.hoppen.cameralib.CallBack.OnWaterListener;

/**
 * Created by YangJianHui on 2021/3/17.
 */
public class HoppenController implements Controller {
    private CameraDevice cameraDevice;
    private McuDevice mcuDevice;


    public HoppenController(OnErrorListener onErrorListener){
        UsbManager usbManager = (UsbManager) Utils.getApp().getSystemService(Context.USB_SERVICE);
        cameraDevice = new CameraDevice(usbManager,onErrorListener);
        mcuDevice = new McuDevice(usbManager);
    }

    public CameraDevice getCameraDevice() {
        return cameraDevice;
    }

    public McuDevice getMcuDevice() {
        return mcuDevice;
    }

//    @Override
//    public void setPreviewSize(int width, int height) {
//
//    }

    @Override
    public Size getPreviewSize() {
        return cameraDevice.getPreviewSize();
    }

    @Override
    public List<Size> getSupportedPreviewSizes() {
        return cameraDevice.getSupportedPreviewSizes();
    }

    public void startPreview() {
        cameraDevice.startPreview();
    }

    public void stopPreview() {
        cameraDevice.stopPreview();
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        cameraDevice.setSurface(new Surface(surfaceTexture));
    }

    @Override
    public void close() {
        cameraDevice.closeDevice();
        mcuDevice.closeDevice();
    }

    @Override
    public void setDeviceButton(OnButtonListener onDeviceButton) {
        cameraDevice.setOnDeviceButton(onDeviceButton);
    }

    @Override
    public void sendInstructions(Instruction instruction) {
        if (cameraDevice.isSpecialDevice()){
            cameraDevice.sendInstructions(instruction);
        }else mcuDevice.sendInstructions(instruction);
    }

    @Override
    public String getDeviceName() {
        return cameraDevice.getCameraName();
    }

    @Override
    public void setContrast(int contrast) {
        cameraDevice.setContrast(contrast);
    }

    @Override
    public void setSaturation(int saturation) {
        cameraDevice.setSaturation(saturation);
    }

    public void setWaterListener(OnWaterListener onWaterListener){
        cameraDevice.setOnWaterListener(onWaterListener);
        mcuDevice.setOnWaterListener(onWaterListener);
    }

    public void setInfoListener(OnInfoListener onInfoListener){
        cameraDevice.setOnInfoListener(onInfoListener);
        mcuDevice.setOnInfoListener(onInfoListener);
    }



}
