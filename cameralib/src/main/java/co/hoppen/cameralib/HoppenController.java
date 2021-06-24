package co.hoppen.cameralib;

import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbManager;
import android.view.Surface;

import com.serenegiant.usb.Size;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;

/**
 * Created by YangJianHui on 2021/3/17.
 */
public class HoppenController implements Controller{
    private CameraDevice cameraDevice;
    private McuDevice mcuDevice;

    public HoppenController(UsbManager usbManager){
        cameraDevice = new CameraDevice(usbManager);
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

//    @Override
//    public void startPreview() {
//        cameraDevice.startPreview();
//    }
//
//    @Override
//    public void stopPreview() {
//        cameraDevice.stopPreview();
//    }

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

    public void setWaterListener(OnWaterListener onWaterListener){
        cameraDevice.setOnWaterListener(onWaterListener);
        mcuDevice.setOnWaterListener(onWaterListener);
    }

    public void setInfoListener(OnInfoListener onInfoListener){
        cameraDevice.setOnInfoListener(onInfoListener);
        mcuDevice.setOnInfoListener(onInfoListener);
    }
}
