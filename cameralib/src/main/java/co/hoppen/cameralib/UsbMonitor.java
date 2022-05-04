package co.hoppen.cameralib;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by YangJianHui on 2021/1/19.
 */
public class UsbMonitor{
    private final static String USB_PERMISSION = co.hoppen.cameralib.UsbMonitor.class.getName();
    private BroadcastReceiver usbReceiver;
    private UsbManager usbManager;
    private List<DeviceFilter> filterList;
    private OnUsbStatusListener onCameraListener;
    private Map<String,UsbDevice> doubleCheckMap = new HashMap<>();//某些平板厂商需要用到doublecheck

    public UsbMonitor(UsbManager usbManager,OnUsbStatusListener onCameraListener){
        if (usbManager!=null && onCameraListener!=null){
            this.onCameraListener = onCameraListener;
            this.usbManager = usbManager;
            filterList = DeviceFilter.getDeviceFilters();
        }
    }

    /**
     *
     * @param remove 是否去除本身自带的设备
     * @param list
     */
    public void addDeviceFilter(boolean remove,List<DeviceFilter> list){
        if (filterList!=null){
            if (remove){
                filterList = new ArrayList<>();
            }
            filterList.addAll(list);
        }
    }

    public void register(Context context){
        initUsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(USB_PERMISSION);
        context.registerReceiver(usbReceiver,filter);
    }

    private void initUsbReceiver() {
        if (usbReceiver==null){
            usbReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                            ||action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)
                            ||action.equals(USB_PERMISSION)){
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        //LogUtils.e(usbDevice.toString());
                        DeviceFilter hoppenDevice = deviceFilter(usbDevice);
                        if (hoppenDevice!=null){
                            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)){
                                if (hasPermission(usbDevice)){//为了适配某些厂商 在插入时就直接有权限
                                    onCameraListener.onConnecting(usbDevice,hoppenDevice.type);
                                }else {
                                    doubleCheckMap.put(usbDevice.getDeviceName(),usbDevice);
                                    requestPermission(context,usbDevice);
                                }
                            }else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
                                onCameraListener.onDisconnect(usbDevice,hoppenDevice.type);
                            }else if (action.equals(USB_PERMISSION)){
                                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                                    doubleCheckMap.remove(usbDevice.getDeviceName());
                                    onCameraListener.onConnecting(usbDevice,hoppenDevice.type);
                                    doubleCheckPermission(context);
                                }else {
                                    requestPermission(context,usbDevice);
                                }
                            }
                        }
                    }

                }
            };
        }
    }

    private void doubleCheckPermission(Context context){
        for (UsbDevice value : doubleCheckMap.values()) {
            requestPermission(context , value);
        }
    }

    public void unregister(Context context){
        if (usbReceiver!=null){
            context.unregisterReceiver(usbReceiver);
            usbReceiver = null;
        }
    }

    /**
     *
     * @param context
     * @param usbDevice
     */
    private void requestPermission(Context context , UsbDevice usbDevice){
        if (usbDevice!=null){
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(context, 0, new Intent(USB_PERMISSION), 0);
            usbManager.requestPermission(usbDevice,pendingIntent);
        }
    }

    private boolean hasPermission(UsbDevice usbDevice){
        if (usbDevice!=null){
            return usbManager.hasPermission(usbDevice);
        }else return false;
    }

    public List<UsbDevice> requestDeviceList(Context context){
        ArrayList<UsbDevice> usbDevices = new ArrayList<>(usbManager.getDeviceList().values());

        Iterator<UsbDevice> iterator = usbDevices.iterator();
        while (iterator.hasNext()){
            UsbDevice next = iterator.next();
            LogUtils.e("request devices pid:"+next.getProductId()+"  vid:"+next.getVendorId());
            DeviceFilter hoppenDevice = deviceFilter(next);
            if (hoppenDevice!=null){
                if (hasPermission(next)){
                    onCameraListener.onConnecting(next,hoppenDevice.type);
                }else {
                    doubleCheckMap.put(next.getDeviceName(),next);
                    requestPermission(context,next);
                }
            }
        }
        return usbDevices;
    }

    private DeviceFilter deviceFilter(UsbDevice usbDevice){
        if (filterList==null) return null;
        Iterator<DeviceFilter> iterator = filterList.iterator();
        while (iterator.hasNext()){
            DeviceFilter deviceFilter = iterator.next();
            if (deviceFilter.mProductId ==usbDevice.getProductId()
                    && deviceFilter.mVendorId ==usbDevice.getVendorId()){
                return deviceFilter;
            }
        }
        return null;
    }

}
