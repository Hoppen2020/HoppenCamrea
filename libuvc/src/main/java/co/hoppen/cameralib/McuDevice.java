package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import co.hoppen.cameralib.CallBack.OnMoistureListener;
import co.hoppen.cameralib.tools.queue.ConnectMcuDeviceTask;
import co.hoppen.cameralib.tools.queue.TaskQueue;

import static co.hoppen.cameralib.CompatibleMode.MODE_AUTO;
import static co.hoppen.cameralib.CompatibleMode.MODE_NONE;
import static co.hoppen.cameralib.CompatibleMode.MODE_SINGLE;

/**
 * Created by YangJianHui on 2022/10/10.
 */
public class McuDevice extends Device{
   private final static int DEFAULT_MAX_READ_BYTES = 128;
   private final static int DEFAULT_TIMEOUT = 300;
   private final TaskQueue taskQueue = new TaskQueue();
   private ConnectMcuDeviceTask.ConnectMcuInfo connectMcuInfo;
   private Thread readDataThread;
//   private OnMoistureListener onMoistureListener;
   private HoppenCamera.CameraConfig cameraConfig;

   private final ThreadUtils.Task<Object> autoSystemOnlineTask = new ThreadUtils.SimpleTask<Object>() {
      @Override
      public Object doInBackground() throws Throwable {
//         LogUtils.e("autoSystemOnlineTask");
         sendInstruction(UsbInstructionUtils.USB_CAMERA_SYS_ONLINE());
         return null;
      }
      @Override
      public void onSuccess(Object result) {
      }
   };

   private final Runnable readRunnable = new Runnable() {
      @Override
      public void run() {
         while (connectMcuInfo!=null && connectMcuInfo.getUsbDeviceConnection()!=null){
            try {
               byte[] bytes = readData();
               if (bytes!=null){
                  ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<Float>() {
                     @Override
                     public Float doInBackground() throws Throwable {
                        String data = decodingData(bytes);
                        return decodingMoisture(data);
                     }

                     @Override
                     public void onSuccess(Float result) {
                        if (cameraConfig.getOnMoistureListener()!=null){
                           if (result>=0){
                              cameraConfig.getOnMoistureListener().onMoistureCallBack(result);
                           }
                        }
                     }
                  });
               }
            }catch (Exception e){
            }
         }
      }
   };


   public McuDevice(HoppenCamera.CameraConfig cameraConfig){
      this.cameraConfig = cameraConfig;
   }

   @Override
   void onConnecting(UsbDevice usbDevice) {
         taskQueue.addTask(new ConnectMcuDeviceTask(usbDevice), new TaskQueue.CurrentTaskFinish<ConnectMcuDeviceTask>() {
            @Override
            public void onFinish(ConnectMcuDeviceTask task) {
               ConnectMcuDeviceTask.ConnectMcuInfo connectMcuInfo = task.getConnectMcuInfo();
               if (connectMcuInfo.isConform()){
                  McuDevice.this.connectMcuInfo = connectMcuInfo;
                  readDataThread = new Thread(readRunnable);
                  readDataThread.start();
               }
            }
         });
   }

   private byte[] readData(){
      if (connectMcuInfo!=null){
         final byte[] data = new byte[DEFAULT_MAX_READ_BYTES];
         int cnt = connectMcuInfo.getUsbDeviceConnection().bulkTransfer(connectMcuInfo.getEpIn(), data, data.length, DEFAULT_TIMEOUT);
         if (cnt!=-1){
            return Arrays.copyOfRange(data, 0, cnt);
         }
      }
      return null;
   }

   private String decodingData(byte [] data) {
      String stringData="";
      try {
         if (connectMcuInfo!=null){
            if (connectMcuInfo.getCompatibleMode()==MODE_AUTO){
               stringData = new String(data);
               stringData = stringData.substring(stringData.indexOf("<[") + 2, stringData.lastIndexOf("]>")).trim();
            }else if (connectMcuInfo.getCompatibleMode()==MODE_SINGLE){
               stringData = data[1]+"-"+data[2];
            }
         }
      }catch (Exception e){
      }
      return stringData;
   }

   private float decodingMoisture(String data){
      float fail = -1;
      try {
         fail = Float.parseFloat(data.replace("-", "."));
      }catch (Exception e){
      }
      return fail;
   }


   @Override
   void onDisconnect(UsbDevice usbDevice) {
      if (connectMcuInfo!=null){
         if (usbDevice.getDeviceName().equals(connectMcuInfo.getDeviceName())){
            closeDevice();
         }
      }
   }

   @Override
   void sendInstruction(Instruction instruction) {
         //异步发送
         ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<Integer>() {
            @Override
            public Integer doInBackground() throws Throwable {
                  CompatibleMode currentMode = connectMcuInfo==null||connectMcuInfo.getCompatibleMode()==null?MODE_NONE:connectMcuInfo.getCompatibleMode();
                  //LogUtils.e("currentMode" + currentMode);
                  if ((currentMode==MODE_NONE || currentMode==MODE_SINGLE)
                          &&
                          instruction != Instruction.MOISTURE) return 0;
                  if (currentMode==MODE_NONE && instruction == Instruction.MOISTURE){
                     return -1;
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
                     case MOISTURE:
                        bytes = currentMode==MODE_AUTO?
                                UsbInstructionUtils.USB_CAMERA_WATER():
                                UsbInstructionUtils.USB_CAMERA_WATER_SINGLE_MODE();
                        break;
                  }
                  sendInstruction(bytes);
               return 0;
            }

            @Override
            public void onSuccess(Integer result) {
               if (cameraConfig.getOnMoistureListener()!=null){
                  if (result==-1){
                     cameraConfig.getOnMoistureListener().onMoistureCallBack(result);
                  }
               }
            }
         });
   }

   private boolean sendInstruction(byte [] data){
      return sendInstruction(data,DEFAULT_TIMEOUT);
   }

   private synchronized boolean sendInstruction(byte [] data,int timeOut){
      boolean success = false;
      if (connectMcuInfo!=null){
         if (connectMcuInfo.getUsbDeviceConnection()!=null&&connectMcuInfo.getEpOut()!=null&&data!=null){
            if (timeOut<=0) timeOut=1000;
            int i= connectMcuInfo.getUsbDeviceConnection().bulkTransfer(connectMcuInfo.getEpOut(), data,data.length,timeOut);
            success = i>0;
         }
      }
      return  success;
   }

   @Override
   void closeDevice() {
      LogUtils.e("mcuDevice closeDevice");
      if (connectMcuInfo != null) {
         try {
            UsbDeviceConnection usbDeviceConnection = connectMcuInfo.getUsbDeviceConnection();
            usbDeviceConnection.releaseInterface(connectMcuInfo.getUsbInterface());
            usbDeviceConnection.close();
            connectMcuInfo.setNull();
         } catch (Exception e) {
         }
      }
   }

   public void startSystemOnline(){
         if (connectMcuInfo!=null){
            LogUtils.e(connectMcuInfo.getCompatibleMode());
            if (connectMcuInfo.getCompatibleMode()== MODE_AUTO){
               ThreadUtils.executeByFixedAtFixRate(1,autoSystemOnlineTask,30,TimeUnit.SECONDS);
            }
         }
   }

   public void stopSystemOnline(){
      ThreadUtils.cancel(autoSystemOnlineTask);
   }
}
