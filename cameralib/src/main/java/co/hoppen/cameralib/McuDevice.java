package co.hoppen.cameralib;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import co.hoppen.cameralib.tools.queue.ConnectMcuDeviceTask;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

import static co.hoppen.cameralib.CompatibleMode.MODE_AUTO;
import static co.hoppen.cameralib.CompatibleMode.MODE_NONE;
import static co.hoppen.cameralib.CompatibleMode.MODE_SINGLE;

/**
 * Created by YangJianHui on 2021/3/16.
 */
public class McuDevice extends HoppenDevice{
    private UsbManager usbManager;
    private UsbDeviceConnection usbDeviceConnection;
    private UsbInterface usbInterface;
    private UsbEndpoint epOut, epIn;
    private final static int DEFAULT_MAX_READ_BYTES = 128;
    private final static int DEFAULT_TIMEOUT = 300;
    private Thread readDataThread;
    private CompatibleMode currentMode;
    private String deviceName;


    private OnWaterListener onWaterListener;
    private Disposable disposable;

    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            while (usbDeviceConnection!=null){
                try {
                    byte[] bytes = readData();
                    if (bytes!=null){
                        LogUtils.e(Arrays.toString(bytes));
                        String data = decodingData(bytes);
                        if (onWaterListener!=null){
                            float water = decodingWater(data);
                            if (water>=0){
                                waterForMainThread(water);
                            }
                        }
                    }
                }catch (Exception e){
                }
            }
        }
    };


    public McuDevice(UsbManager usbManager){
        this.usbManager = usbManager;
    }

    private void waterForMainThread(float water){
        Observable.just(water).observeOn(AndroidSchedulers.mainThread()).subscribe(aFloat ->
                onWaterListener.onWaterCallback(aFloat.floatValue()));
    }

    public synchronized void onConnecting(ConnectMcuDeviceTask.ConnectMcuInfo connectMcuInfo){
        currentMode = connectMcuInfo.getCompatibleMode();
        usbDeviceConnection = connectMcuInfo.getUsbDeviceConnection();
        usbInterface = connectMcuInfo.getUsbInterface();
        epOut = connectMcuInfo.getEpOut();
        epIn = connectMcuInfo.getEpIn();
        if (connectMcuInfo.getCompatibleMode()== MODE_AUTO){
            disposable = Observable.interval(5, 30, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) throws Throwable {
                    sendInstructions(UsbInstructionUtils.USB_CAMERA_SYS_ONLINE());
                }
            });
        }
        readDataThread = new Thread(readRunnable);
        readDataThread.start();
    }

    @Override
    public synchronized void onConnecting(UsbDevice usbDevice, DeviceType type) {

    }

    @Override
    public void setOnWaterListener(OnWaterListener onWaterListener) {
        this.onWaterListener = onWaterListener;
    }

    @Override
    protected void setOnInfoListener(OnInfoListener onInfoListener) {

    }

    @Override
    public synchronized void onDisconnect(UsbDevice usbDevice, DeviceType type) {
        if (deviceName!=null && deviceName.equals(usbDevice.getDeviceName())){
            this.closeDevice();
        }
    }

    @Override
    public void sendInstructions(Instruction instruction){
        Observable.just(instruction)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Instruction>() {
            @Override
            public void accept(Instruction instruction) throws Throwable {
                if ((currentMode==MODE_NONE || currentMode==MODE_SINGLE)
                        &&
                        instruction != Instruction.WATER) return;
                if (currentMode==MODE_NONE && instruction == Instruction.WATER){
                    waterForMainThread(-1);
                    return;
                }
                byte [] bytes = null;
                switch (instruction){
                    case LIGHT_CLOSE:
                        bytes = UsbInstructionUtils.USB_CAMERA_LIGHT_CLOSE();
                        break;
                    case LIGHT_UV:
                        bytes = UsbInstructionUtils.USB_CAMERA_LIGHT_UV();
                        break;
                    case LIGHT_RGB:
                        bytes = UsbInstructionUtils.USB_CAMERA_LIGHT_RGB();
                        break;
                    case LIGHT_POLARIZED:
                        bytes = UsbInstructionUtils.USB_CAMERA_LIGHT_POLARIZED();
                        break;
                    case PRODUCT_CODE:
                        bytes = UsbInstructionUtils.USB_CAMERA_PRODUCT_CODE();
                        break;
                    case SYS_ONLINE:
                        bytes = UsbInstructionUtils.USB_CAMERA_SYS_ONLINE();
                        break;
                    case WATER:
                        bytes = currentMode==MODE_AUTO?
                                UsbInstructionUtils.USB_CAMERA_WATER():
                                UsbInstructionUtils.USB_CAMERA_WATER_SINGLE_MODE();
                        break;
                }
                sendInstructions(bytes);
            }
        },throwable -> {
            //错误信息
        });
    }

    @Override
    protected void closeDevice() {
        if (usbDeviceConnection!=null){
            try {
                if (deviceName!=null){
                    currentMode = MODE_NONE;
                    if (disposable!=null)disposable.dispose();
                    usbDeviceConnection.releaseInterface(usbInterface);
                    usbDeviceConnection.close();
                    usbDeviceConnection=null;
                    usbInterface = null;
                    epOut = null;
                    epIn = null;
                    deviceName = null;
                }
            }catch (Exception e){
            }
        }
    }

    private synchronized boolean sendInstructions(byte [] data,int timeOut){
        boolean success = false;
            if (usbDeviceConnection!=null&&epOut!=null&&data!=null){
                if (timeOut<=0) timeOut=1000;
                int i= usbDeviceConnection.bulkTransfer(epOut,data,data.length,timeOut);
                success = i>0;
            }
        return  success;
    }

    private byte[] readData(){
        final byte[] data = new byte[DEFAULT_MAX_READ_BYTES];
        int cnt = usbDeviceConnection.bulkTransfer(epIn, data, data.length, DEFAULT_TIMEOUT);
        if (cnt!=-1){
            byte[] bytes = Arrays.copyOfRange(data, 0, cnt);
            return bytes;
        }else return null;
    }

    private boolean sendInstructions(byte [] data){
        return sendInstructions(data,DEFAULT_TIMEOUT);
    }

    private float decodingWater(String data){
        float fail = -1;
        try {
            fail = Float.parseFloat(data.replace("-", "."));
        }catch (Exception e){
        }
        return fail;
    }

    private String decodingData(byte [] data) {
        String stringData="";
        try {
            if (currentMode==MODE_AUTO){
                stringData = new String(data);
                stringData = stringData.substring(stringData.indexOf("<[") + 2, stringData.lastIndexOf("]>")).trim();
            }else if (currentMode==MODE_SINGLE){
                stringData = data[1]+"-"+data[2];
            }
        }catch (Exception e){
        }
        return stringData;
    }

}
