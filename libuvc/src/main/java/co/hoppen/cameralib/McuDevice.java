package co.hoppen.cameralib;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
   private final HoppenCamera.CameraConfig cameraConfig;
   private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

   private final Runnable systemOnlineRunnable = new Runnable() {
      @Override
      public void run() {
         if (connectMcuInfo!=null && connectMcuInfo.getUsbDeviceConnection()!=null){
            sendInstruction(UsbInstructionUtils.USB_CAMERA_SYS_ONLINE());
         }else throw new RuntimeException();
      }
   };
   private final Runnable readRunnable = new Runnable() {
      @Override
      public void run() {
         while (connectMcuInfo!=null && connectMcuInfo.getUsbDeviceConnection()!=null){
            try {
               byte[] bytes = readData();
               if (bytes!=null){
                  String data = decodingData(bytes);
                  //LogUtils.e(data);
                  float result = decodingMoisture(data);
                  //LogUtils.e(result);
                  ThreadUtils.executeBySingle(new ThreadUtils.SimpleTask<Float>() {
                     @Override
                     public Float doInBackground() throws Throwable {
                        return result;
                     }

                     @Override
                     public void onSuccess(Float result) {
                        if (result>=0){
                           if (cameraConfig.getOnMoistureListener()!=null){
                              //LogUtils.e(result);
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
                  LogUtils.e(connectMcuInfo.getCompatibleMode());
                  if (connectMcuInfo.getCompatibleMode() == MODE_AUTO){
                     executorService.scheduleAtFixedRate(systemOnlineRunnable,0,5,TimeUnit.SECONDS);
                  }
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
         if (data.contains("-")){
            int index = data.indexOf("-");
            //部分机器粘包问题，处理兼容
            data = data.substring(index-2,index+3);
            fail = Float.parseFloat(data.replace("-", "."));
         }
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
      sendInstruction(instruction,0);
   }

   public void sendInstruction(Instruction instruction,int count){
      //异步发送
      ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<Integer>() {
         @Override
         public Integer doInBackground() throws Throwable {
            CompatibleMode currentMode = connectMcuInfo==null||
                    connectMcuInfo.getCompatibleMode()==null?MODE_NONE:connectMcuInfo.getCompatibleMode();
            LogUtils.e("currentMode" + currentMode , count);
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

            if (count!=0){
               //LogUtils.e("send count "+count);
               for (int i = 0; i < count; i++) {
                  boolean state = sendInstruction(bytes);
                  //LogUtils.e("count "+i + "  "+state);
                  if (state){
                     break;
                  }
               }
            }else {
               sendInstruction(bytes);
            }
            return 0;
         }

         @Override
         public void onSuccess(Integer result) {
            //-1 就是没有获取电阻的设备
            if (result==-1){
               if (cameraConfig.getOnMoistureListener()!=null)cameraConfig.getOnMoistureListener().onMoistureCallBack(result);
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
      if (connectMcuInfo != null) {
         try {
//            taskQueue.cancel();
//            ThreadUtils.cancel(autoSystemOnlineTask);
            UsbDeviceConnection usbDeviceConnection = connectMcuInfo.getUsbDeviceConnection();
            usbDeviceConnection.releaseInterface(connectMcuInfo.getUsbInterface());
            usbDeviceConnection.close();
            connectMcuInfo.setNull();
         } catch (Exception e) {
         }
      }
   }
}
