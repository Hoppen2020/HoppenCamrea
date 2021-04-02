package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Surface;

import com.blankj.utilcode.util.LogUtils;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import java.util.List;

/**
 * Created by YangJianHui on 2021/3/16.
 */
public class CameraDevice extends HoppenDevice implements IButtonCallback {
    private ControlBlock controlBlock;
    private UsbManager usbManager;
    private UVCCamera uvcCamera;
    private Surface surface;
    private String deviceName;
    private OnButtonListener onDeviceButton;
    private int width = DEFAULT_WIDTH,height = DEFAULT_HEIGHT;
    private final static int DEFAULT_WIDTH = 800;
    private final static int DEFAULT_HEIGHT = 600;
    private String cameraName = "";

    public CameraDevice(UsbManager usbManager){
        this.usbManager = usbManager;
    }

    public void setPreviewDisplay(Surface surface){
        if (uvcCamera!=null){
            uvcCamera.setPreviewDisplay(surface);
        }
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    @Override
    public void onConnecting(UsbDevice usbDevice, DeviceType type) {
//        LogUtils.e(usbDevice);
        if (deviceName==null){
            deviceName = usbDevice.getDeviceName();
        }else {
            if (!deviceName.equals(usbDevice.getDeviceName())){
                return;
            }
        }
        createPreviewSize(usbDevice.getProductName());
        cameraName = usbDevice.getProductName();
        if (controlBlock==null){
            controlBlock = new ControlBlock(usbManager,usbDevice);
            if (controlBlock.open()!=null){
                try {
                    uvcCamera = new UVCCamera();
                    uvcCamera.open(controlBlock);
                    uvcCamera.setPreviewSize(width, height);
                    startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDisconnect(UsbDevice usbDevice, DeviceType type) {
        if (deviceName!= null && deviceName.equals(usbDevice.getDeviceName())){
            cameraName = "";
            this.closeDevice();
        }
    }

    public void startPreview(){
        try {
            if (uvcCamera!=null){
                if ( surface!= null) {
                    uvcCamera.setButtonCallback(CameraDevice.this);
                    uvcCamera.setPreviewDisplay(surface);
                }
                uvcCamera.updateCameraParams();
                uvcCamera.startPreview();
            }
        }catch (Exception e){
        }
    }

    public void stopPreview(){
        try {
            if (uvcCamera!=null){
                uvcCamera.setButtonCallback(null);
                uvcCamera.setFrameCallback(null, 0);
                uvcCamera.stopPreview();
            }
        }catch (Exception e){
        }
    }

    @Override
    protected void closeDevice() {
        try {
            if (deviceName!=null){
                if (uvcCamera != null) {
                    uvcCamera.destroy();
                    uvcCamera = null;
                }
                if (controlBlock!=null){
                    controlBlock.close();
                    controlBlock = null;
                }
                deviceName = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onButton(int button, int state) {
        if (onDeviceButton!=null)onDeviceButton.onButton(state);
    }

    public void setOnDeviceButton(OnButtonListener onDeviceButton) {
        this.onDeviceButton = onDeviceButton;
    }

    public List<Size> getSupportedPreviewSizes(){
        try {
            return uvcCamera.getSupportedSizeList();
        }catch (Exception e){
        }
        return null;
    }

    public Size getPreviewSize(){
        try {
            return uvcCamera.getPreviewSize();
        }catch (Exception e){
        }
        return null;
    }

    private void createPreviewSize(String productName){
        if (productName.equals("WAX-04+80")){
            width = 800;
            height = 600;
        }else if (productName.equals("WAX-PF4D3-MK")){
            width = 1280;
            height = 720;
        }else if (productName.equals("WAX-PF4D2-SX")){
            width = 800;
            height = 600;
        }else if (productName.equals("WAX-PF4D3-SX")){
            width = 800;
            height = 600;
        }
    }

    public String getCameraName() {
        return cameraName==null?"":cameraName;
    }
}
