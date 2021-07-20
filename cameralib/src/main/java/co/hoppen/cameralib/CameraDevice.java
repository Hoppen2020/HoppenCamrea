package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.view.Surface;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.serenegiant.usb.IButtonCallback;
import com.serenegiant.usb.Size;
import com.serenegiant.usb.UVCCamera;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

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
        //LogUtils.e(usbDevice.toString(),usbDevice.getDeviceName(),usbDevice.getProductName());
        if (deviceName==null){
            deviceName = usbDevice.getDeviceName();
        }else {
            if (!deviceName.equals(usbDevice.getDeviceName())){
                return;
            }
        }
        cameraName = usbDevice.getProductName();
        if (cameraName==null){
            //可因usbhub导致 快速插拔 影响 无法获取device信息
            LogUtils.e(usbDevice.toString());
            return;
        }
        createPreviewSize(cameraName);
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
            }else {
                //LogUtils.e("null null null");
                cameraName = "";
                specialDevice = false;
                this.closeDevice();
            }
        }
    }

    @Override
    public void onDisconnect(UsbDevice usbDevice, DeviceType type) {
        if (deviceName!= null && deviceName.equals(usbDevice.getDeviceName())){
            cameraName = "";
            specialDevice = false;
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
    protected void sendInstructions(Instruction instruction) {
        if (!specialDevice && uvcCamera!=null)return;
        Observable.just(instruction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Instruction>() {
                    @Override
                    public void accept(Instruction instruction) throws Throwable {
                        int writeCmd = 0x82;
                        int readCmd = 0xc2;
                        int writeAddr = 0xd55b;
                        int readAddr = 0xd55c;
                        int send = -1;
                        byte[] pdat = new byte[4];
                        pdat[0] = 0x0;    // 0 for write, 1 for read
                        pdat[1] = 0x78;    // slave id (same for read and write)
                        pdat[2] = 0;
                        pdat[3] = 0;
                        switch (instruction) {
                            case LIGHT_CLOSE:
                                pdat[2] = 0x10;
                                pdat[3] = 0x00;
                                send = uvcCamera.nativeXuWrite(writeCmd, writeAddr, pdat.length, pdat);
                                break;
                            case LIGHT_UV:
                                pdat[2] = 0x13;
                                pdat[3] = (byte) 0xff;
                                send = uvcCamera.nativeXuWrite(writeCmd, writeAddr, pdat.length, pdat);
                                break;
                            case LIGHT_RGB:
                                pdat[2] = 0x11;
                                pdat[3] = (byte) 0xff;
                                send = uvcCamera.nativeXuWrite(writeCmd, writeAddr, pdat.length, pdat);
                                break;
                            case LIGHT_POLARIZED:
                                pdat[2] = 0x12;
                                pdat[3] = (byte) 0xff;
                                send = uvcCamera.nativeXuWrite(writeCmd, writeAddr, pdat.length, pdat);
                                break;
                            case WATER:
                                pdat[0] = 0x1;    // 0 for write, 1 for read
                                pdat[1] = 0x78;
                                pdat[2] = 0x79;
                                uvcCamera.nativeXuWrite(writeCmd, writeAddr, 4, pdat);
                                uvcCamera.nativeXuRead(readCmd, readAddr, 4, pdat);
                                //ToastUtils.showShort(Arrays.toString(pdat));
                                            if (onWaterListener != null) {
                                                int a = pdat[0];
                                                int b = pdat[1];
                                                int c = pdat[2];
                                                int d = a^b;
                                                if (c == d){
                                                    float water = Float.parseFloat(a+ "." + b);
                                                    if (water >= 0) {
                                                        onWaterListener.onWaterCallback(water);
                                                    }
                                                }
                                            }
                                uvcCamera.setBrightness(0);
                                break;
                            case UNIQUE_CODE:
                                byte[] pbuf1 = new byte[4];
                                uvcCamera.nativeXuRead(0x02c2, 0xFE00, 4, pbuf1);
                                if (pbuf1[0] == 0xff && pbuf1[1] == 0xff && pbuf1[2] == 0xff && pbuf1[3] == 0xff) {
                                    //nothing work
                                } else {
                                    int len = (pbuf1[0] << 24) + (pbuf1[1] << 16) + (pbuf1[2] << 8) + pbuf1[3] - 4;
                                    byte[] pbuf2 = new byte[len];
                                    LogUtils.e(len);
                                    uvcCamera.nativeXuRead(0x02c2, 0xFE00 + 4, len, pbuf2);
                                    if (onInfoListener != null) {
                                        String info = new String(pbuf2, 0, 12);
                                        onInfoListener.onInfoCallback(instruction, info);
                                    }
                                }
                                break;
                        }
                    }
                }, throwable -> {
                    //错误信息
                });
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
                deviceName = null;
                if (uvcCamera != null) {
                    uvcCamera.destroy();
                    uvcCamera = null;
                }
                if (controlBlock!=null){
                    controlBlock.close();
                    controlBlock = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onButton(int button, int state) {
        if (onDeviceButton!=null){
            //解决 sxw手柄在某平板卡视频流（无解）
            ThreadUtils.runOnUiThread(() -> onDeviceButton.onButton(state));
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
        if (!StringUtils.isEmpty(productName)){
            if (productName.equals("WAX-04+80")){
                width = 640;
                height = 480;
            }else if (productName.equals("WAX-PF4D3-MK")){
                width = 800;
                height = 600;
            }else if (productName.equals("WAX-PF4D2-SX")){
                width = 640;
                height = 480;
                specialDevice = true;
            }else if (productName.equals("WAX-PF4D3-SX")){
                width = 1280;
                height = 960;
                specialDevice = true;
            }
        }
    }

    public String getCameraName() {
        return cameraName==null?"":cameraName;
    }


    public boolean isSpecialDevice() {
        return specialDevice;
    }
}
