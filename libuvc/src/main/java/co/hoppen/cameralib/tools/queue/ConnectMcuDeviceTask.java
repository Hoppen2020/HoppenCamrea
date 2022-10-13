package co.hoppen.cameralib.tools.queue;

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;

import java.util.Arrays;

import co.hoppen.cameralib.CompatibleMode;
import co.hoppen.cameralib.UsbInstructionUtils;

import static co.hoppen.cameralib.CompatibleMode.MODE_AUTO;
import static co.hoppen.cameralib.CompatibleMode.MODE_NONE;
import static co.hoppen.cameralib.CompatibleMode.MODE_SINGLE;

/**
 * Created by YangJianHui on 2022/5/4.
 */
public class ConnectMcuDeviceTask extends Task{
   private ConnectMcuInfo connectMcuInfo;

   public ConnectMcuDeviceTask(UsbDevice usbDevice){
      connectMcuInfo = new ConnectMcuInfo(usbDevice);
   }

   public ConnectMcuInfo getConnectMcuInfo() {
      return connectMcuInfo;
   }

   @Override
   public void taskContent() {
      if (connectMcuInfo!=null){
         UsbManager usbManager = (UsbManager) Utils.getApp().getSystemService(Context.USB_SERVICE);
         UsbDevice usbDevice = connectMcuInfo.usbDevice;
         UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
         int interfaceCount = usbDevice.getInterfaceCount();
         LogUtils.e("interface count :"+interfaceCount,usbDevice.toString());
         if (usbDeviceConnection!=null && interfaceCount>0) {
            UsbInterface usbInterface;
            UsbEndpoint epOut = null,epIn = null;

            usbInterface = usbDevice.getInterface(interfaceCount - 1);

            boolean claimInterface = usbDeviceConnection.claimInterface(usbInterface, false);
//            LogUtils.e(claimInterface);
            if (claimInterface) {
               //设置波特率等设置
               setConfig(usbDeviceConnection, 9600, 8, 1, 0);
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
               connectMcuInfo.usbDeviceConnection = usbDeviceConnection;
               connectMcuInfo.usbInterface = usbInterface;
               connectMcuInfo.epOut = epOut;
               connectMcuInfo.epIn = epIn;

               //-----------
               CompatibleMode mode = CompatibleMode.MODE_NONE;

               mode = discernMode(UsbInstructionUtils.USB_CAMERA_PRODUCT_CODE(),connectMcuInfo);
               if (mode != MODE_AUTO){
                  mode = discernMode(UsbInstructionUtils.USB_CAMERA_WATER_SINGLE_MODE(),connectMcuInfo);
               }
               connectMcuInfo.compatibleMode = mode;
               connectMcuInfo.conform = mode!=MODE_NONE;

//               LogUtils.e(connectMcuInfo.conform,mode);

               if (mode==MODE_NONE){
                  try {
                     usbDeviceConnection.releaseInterface(usbInterface);
                     usbDeviceConnection.close();
                     connectMcuInfo.usbDeviceConnection = usbDeviceConnection=null;
                     connectMcuInfo.usbInterface = usbInterface = null;
                     connectMcuInfo.epOut = epOut = null;
                     connectMcuInfo.epIn  = epIn = null;
                  }catch (Exception e){
                  }
               }
            }
         }
      }
   }

   private synchronized boolean sendInstructions(byte [] data , ConnectMcuInfo connectMcuInfo){
      boolean success = false;
      if (connectMcuInfo.usbDeviceConnection!=null&&connectMcuInfo.epOut!=null&&data!=null){
         int i= connectMcuInfo.usbDeviceConnection.bulkTransfer(connectMcuInfo.epOut,data,data.length,300);
         success = i>0;
      }
      return  success;
   }

   private CompatibleMode discernMode(byte [] data,ConnectMcuInfo connectMcuInfo){
      CompatibleMode mode = CompatibleMode.MODE_NONE;
      boolean success = sendInstructions(data,connectMcuInfo);
//      LogUtils.e(success);
      if (success){
         byte[] bytes = readData(connectMcuInfo);
         if (bytes!=null){
            try {
               String judge = new String(bytes);
               if (judge.contains("Command-Invalid")){
                  mode = MODE_NONE;
               }else if (Arrays.equals(data,UsbInstructionUtils.USB_CAMERA_PRODUCT_CODE())){
                  mode = MODE_AUTO;
               }else if (Arrays.equals(data,UsbInstructionUtils.USB_CAMERA_WATER_SINGLE_MODE())){
                  mode = MODE_SINGLE;
               }
            }catch (Exception e){
               LogUtils.e(e.toString());
            }
         }
      }
      return mode;
   }

   private byte[] readData(ConnectMcuInfo connectMcuInfo){
      final byte[] data = new byte[128];
      int cnt = connectMcuInfo.usbDeviceConnection.bulkTransfer(connectMcuInfo.epIn, data, data.length, 300);
      if (cnt!=-1){
         byte[] bytes = Arrays.copyOfRange(data, 0, cnt);
         return bytes;
      }else return null;
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


   public class ConnectMcuInfo{
      private UsbInterface usbInterface = null;
      private UsbEndpoint epOut = null,epIn = null;
      private UsbDeviceConnection usbDeviceConnection = null;
      private boolean conform = false;
      private CompatibleMode compatibleMode;
      private UsbDevice usbDevice;

      public ConnectMcuInfo(UsbDevice usbDevice){
         this.usbDevice = usbDevice;
      }

      public UsbInterface getUsbInterface() {
         return usbInterface;
      }

      public UsbEndpoint getEpOut() {
         return epOut;
      }

      public UsbEndpoint getEpIn() {
         return epIn;
      }

      public UsbDeviceConnection getUsbDeviceConnection() {
         return usbDeviceConnection;
      }

      public boolean isConform() {
         return conform;
      }

      public CompatibleMode getCompatibleMode() {
         return compatibleMode;
      }

      public String getDeviceName(){
         if (usbDevice!=null){
            return usbDevice.getDeviceName();
         }else return "";
      }

      public void setNull(){
         usbDeviceConnection = null;
         usbInterface = null;
         epOut = null;
         epIn = null;
         usbDevice = null;
      }

   }


}
