package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Surface;

import com.blankj.utilcode.util.LogUtils;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;

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
    private boolean specialDevice = false;
    private OnWaterListener onWaterListener;
    private OnInfoListener onInfoListener;

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
        //LogUtils.e(usbDevice.toString());
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
    protected boolean sendInstructions(Instruction instruction) {
        if (!specialDevice && uvcCamera!=null)return false;
        int writeCmd = 0x82;
        int readCmd = 0xc2;
        int writeAddr = 0xd55b;
        int readAddr = 0xd55c;
        int send = -1;
        byte [] pdat = new byte[4];
        pdat[0] = 0x0;	// 0 for write, 1 for read
        pdat[1] = 0x78;	// slave id (same for read and write)
        pdat[2] = 0;
        pdat[3] = 0;
        switch (instruction){
            case LIGHT_CLOSE:
                pdat[2] = 0x10;
                pdat[3] = 0x00;
                send = uvcCamera.nativeXuWrite(writeCmd,writeAddr,pdat.length,pdat);
                break;
            case LIGHT_UV:
                pdat[2] = 0x13;
                pdat[3] = (byte) 0xff;
                send = uvcCamera.nativeXuWrite(writeCmd,writeAddr,pdat.length,pdat);
                break;
            case LIGHT_RGB:
                pdat[2] = 0x11;
                pdat[3] = (byte) 0xff;
                send = uvcCamera.nativeXuWrite(writeCmd,writeAddr,pdat.length,pdat);
                break;
            case LIGHT_POLARIZED:
                pdat[2] = 0x12;
                pdat[3] = (byte) 0xff;
                send = uvcCamera.nativeXuWrite(writeCmd,writeAddr,pdat.length,pdat);
                break;
            case WATER:
                pdat[0] = 0x1;	// 0 for write, 1 for read
                pdat[1] = 0x79;
                pdat[2] = 0;
                uvcCamera.nativeXuWrite(writeCmd, writeAddr, 4, pdat);
                uvcCamera.nativeXuRead(readCmd, readAddr, 3, pdat);
                LogUtils.e("new camera" +  Arrays.toString(pdat));
                break;
            case UNIQUE_CODE:
                pdat = new byte[12];
                uvcCamera.nativeXuRead(0x02c2, 0xFE00, pdat.length, pdat);
                LogUtils.e(Arrays.toString(pdat));
                LogUtils.e(new String(pdat));
//                byte[] a = ConvertUtils.string2Bytes("SXB0100001");
//                LogUtils.e(new String(a));
//                LogUtils.e(Arrays.toString(a));
//                LogUtils.e(ConvertUtils.bytes2String(a));
                if (onInfoListener!=null){
                    Observable.just(new String(pdat)).observeOn(AndroidSchedulers.mainThread()).subscribe(info ->
                                onInfoListener.onInfoCallback(instruction, info)
                            );
                }
                break;
        }
        return send!=-1;
    }

    @Override
    protected void setOnWaterListener(OnWaterListener onWaterListener) {
        this.onWaterListener = onWaterListener;
    }

    @Override
    protected void setOnInfoListener(OnInfoListener onInfoListener) {
        this.onInfoListener = onInfoListener;
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
        if (onDeviceButton!=null){
            Observable.just(state).observeOn(AndroidSchedulers.mainThread()).subscribe(integer -> onDeviceButton.onButton(state));
        }
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
        LogUtils.e(productName);
        if (productName.equals("WAX-04+80")){
            width = 800;
            height = 600;
        }else if (productName.equals("WAX-PF4D3-MK")){
            width = 800;
            height = 600;
        }else if (productName.equals("WAX-PF4D2-SX")){
            width = 640;
            height = 480;
            specialDevice = true;
        }else if (productName.equals("WAX-PF4D3-SX")){
            width = 640;
            height = 480;
            specialDevice = true;
        }
    }

    public String getCameraName() {
        return cameraName==null?"":cameraName;
    }


    public boolean isSpecialDevice() {
        return specialDevice;
    }
}
