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

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;

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
    private String deviceName;
    private final static int MODE_AUTO = 1;
    private final static int MODE_SINGLE = 2;
    private final static int MODE_NONE = 0;
    private int currentMode = MODE_NONE;
    private Thread readDataThread;

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


    @Override
    public synchronized void onConnecting(UsbDevice usbDevice, DeviceType type) {
        if (deviceName==null){
            deviceName = usbDevice.getDeviceName();
        }else {
            if (!deviceName.equals(usbDevice.getDeviceName()))return;
        }
        usbDeviceConnection = usbManager.openDevice(usbDevice);
        int interfaceCount = usbDevice.getInterfaceCount();
        if (usbDeviceConnection!=null && interfaceCount>0){
            usbInterface = usbDevice.getInterface(interfaceCount - 1);
            boolean claimInterface = usbDeviceConnection.claimInterface(usbInterface, false);
            if (claimInterface){
                //设置波特率等设置
                setConfig(usbDeviceConnection,9600,8,1,0);
                for (int index = 0; index < usbInterface.getEndpointCount(); index++) {
                    UsbEndpoint ep = usbInterface.getEndpoint(index);
                    if ((ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                            && (ep.getDirection() == UsbConstants.USB_DIR_OUT)) {
                        epOut = ep;
                    }
                    if ((ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                            && (ep.getDirection() == UsbConstants.USB_DIR_IN)) {
                        epIn = ep;
                    }
                }
                //-----------
                currentMode = discernMode(UsbInstructionUtils.USB_CAMERA_PRODUCT_CODE());
                if (currentMode==MODE_AUTO){
                    disposable = Observable.interval(5, 30, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Throwable {
                            sendInstructions(UsbInstructionUtils.USB_CAMERA_SYS_ONLINE());
                        }
                    });
                }else {
                    currentMode = discernMode(UsbInstructionUtils.USB_CAMERA_WATER_SINGLE_MODE());
                }
                if (currentMode == MODE_NONE)closeDevice();
                    readDataThread = new Thread(readRunnable);
                    readDataThread.start();
            }
        }

    }

    private int discernMode(byte [] data){
        int mode = MODE_NONE;
        boolean success = sendInstructions(data);
        if (success){
            byte[] bytes = readData();
            if (bytes!=null){
                if (Arrays.equals(data,UsbInstructionUtils.USB_CAMERA_PRODUCT_CODE())){
                    mode = MODE_AUTO;
                }else if (Arrays.equals(data,UsbInstructionUtils.USB_CAMERA_WATER_SINGLE_MODE())){
                    mode = MODE_SINGLE;
                }
            }
        }
        return mode;
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


    private void setConfig(UsbDeviceConnection connection, int baudRate, int dataBit, int stopBit, int parity) {
        int value = 0;
        int index = 0;
        char valueHigh = 0, valueLow = 0, indexHigh = 0, indexLow = 0;
        switch (parity) {
            case 0: /* NONE */
                valueHigh = 0x00;
                break;
            case 1: /* ODD */
                valueHigh |= 0x08;
                break;
            case 2: /* Even */
                valueHigh |= 0x18;
                break;
            case 3: /* Mark */
                valueHigh |= 0x28;
                break;
            case 4: /* Space */
                valueHigh |= 0x38;
                break;
            default: /* None */
                valueHigh = 0x00;
                break;
        }

        if (stopBit == 2) {
            valueHigh |= 0x04;
        }

        switch (dataBit) {
            case 5:
                valueHigh |= 0x00;
                break;
            case 6:
                valueHigh |= 0x01;
                break;
            case 7:
                valueHigh |= 0x02;
                break;
            case 8:
                valueHigh |= 0x03;
                break;
            default:
                valueHigh |= 0x03;
                break;
        }

        valueHigh |= 0xc0;
        valueLow = 0x9c;

        value |= valueLow;
        value |= (int) (valueHigh << 8);

        switch (baudRate) {
            case 50:
                indexLow = 0;
                indexHigh = 0x16;
                break;
            case 75:
                indexLow = 0;
                indexHigh = 0x64;
                break;
            case 110:
                indexLow = 0;
                indexHigh = 0x96;
                break;
            case 135:
                indexLow = 0;
                indexHigh = 0xa9;
                break;
            case 150:
                indexLow = 0;
                indexHigh = 0xb2;
                break;
            case 300:
                indexLow = 0;
                indexHigh = 0xd9;
                break;
            case 600:
                indexLow = 1;
                indexHigh = 0x64;
                break;
            case 1200:
                indexLow = 1;
                indexHigh = 0xb2;
                break;
            case 1800:
                indexLow = 1;
                indexHigh = 0xcc;
                break;
            case 2400:
                indexLow = 1;
                indexHigh = 0xd9;
                break;
            case 4800:
                indexLow = 2;
                indexHigh = 0x64;
                break;
            case 9600:
                indexLow = 2;
                indexHigh = 0xb2;
                break;
            case 19200:
                indexLow = 2;
                indexHigh = 0xd9;
                break;
            case 38400:
                indexLow = 3;
                indexHigh = 0x64;
                break;
            case 57600:
                indexLow = 3;
                indexHigh = 0x98;
                break;
            case 115200:
                indexLow = 3;
                indexHigh = 0xcc;
                break;
            case 230400:
                indexLow = 3;
                indexHigh = 0xe6;
                break;
            case 460800:
                indexLow = 3;
                indexHigh = 0xf3;
                break;
            case 500000:
                indexLow = 3;
                indexHigh = 0xf4;
                break;
            case 921600:
                indexLow = 7;
                indexHigh = 0xf3;
                break;
            case 1000000:
                indexLow = 3;
                indexHigh = 0xfa;
                break;
            case 2000000:
                indexLow = 3;
                indexHigh = 0xfd;
                break;
            case 3000000:
                indexLow = 3;
                indexHigh = 0xfe;
                break;
            default: // default baudRate "9600"
                indexLow = 2;
                indexHigh = 0xb2;
                break;
        }

        index |= 0x88 | indexLow;
        index |= (int) (indexHigh << 8);

        connection.controlTransfer(
                (0x02 << 5) | 0x00 | 0x00, 0xA1, value, index, null,
                0, 2000);
    }


}
