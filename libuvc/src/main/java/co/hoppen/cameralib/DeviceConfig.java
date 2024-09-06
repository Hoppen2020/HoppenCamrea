package co.hoppen.cameralib;

import co.hoppen.cameralib.compatible.SpecialUsbTag;

/**
 * Created by YangJianHui on 2022/10/8.
 */
public enum DeviceConfig {
   WAX_04_80("WAX-04+80",640,480,CommunicationType.MCU),
   WAX_PF4D1_MK("WAX-PF4D1-MK",640,480,CommunicationType.MCU),
   WAX_PF4D3_MK("WAX-PF4D3-MK",800,600,CommunicationType.MCU),
   WAX_FACE_USB_OLD("USB 2.0 Camera",1600,1200,CommunicationType.MCU),
   WAX_PF4D2_SX("WAX-PF4D2-SX",640,480,CommunicationType.INTERNAL_THREE_LIGHT),
   WAX_PF4D3_SX("WAX-PF4D3-SX",1280,960,CommunicationType.INTERNAL_THREE_LIGHT),
   WAX_PF4D4_SX("WAX_PF4D4_SX",1280,960,CommunicationType.INTERNAL_THREE_LIGHT),
   WAX_PF3H1_SX("WAX-PF3H1-SX",1280,960,CommunicationType.INTERNAL_THREE_LIGHT),
   WAX_FACE_USB3("USB3.0",1920,1080,CommunicationType.INTERNAL_FIVE_LIGHT),
   WAX_DETECT_FACE("USB Camera",1920,1080,CommunicationType.NONE),
   //皮肤手柄(头皮改)
   WAX_PF3D1_SX("WAX-PF3D1-SX",1280,960,CommunicationType.INTERNAL_THREE_LIGHT),
   //旧版全脸
   WAX_PF3F2_SX("WAX-PF3F2-SX",1920,1080,CommunicationType.INTERNAL_FIVE_LIGHT),
   //WAX_FUNCTION("TEST",640,480,CommunicationType.NONE),

   WAX_DEFAULT("default",640,480,CommunicationType.MCU);


   private String deviceName;
   private int resolutionWidth;
   private int resolutionHeight;
   private CommunicationType communicationType;
   DeviceConfig(String name,int resolutionWidth, int resolutionHeight,CommunicationType communicationType){
      this.deviceName = name;
      this.resolutionWidth = resolutionWidth;
      this.resolutionHeight = resolutionHeight;
      this.communicationType = communicationType;
   }

   public String getDeviceName() {
      return deviceName;
   }

   public int getResolutionWidth() {
      return resolutionWidth;
   }

   public int getResolutionHeight() {
      return resolutionHeight;
   }

   public CommunicationType getCommunicationType() {
      return communicationType;
   }

   public boolean isMcuCommunication(){
      return communicationType==CommunicationType.MCU;
   }

   public static DeviceConfig getDeviceConfig(String deviceName){
      if (deviceName.equals("WAX-04+80")){
         return WAX_04_80;
      }else if (deviceName.equals("WAX-PF4D1-MK")){
         return WAX_PF4D1_MK;
      }else if (deviceName.equals("WAX-PF4D3-MK")){
         return WAX_PF4D3_MK;
      }else if (deviceName.equals("WAX-PF4D2-SX")){
         return WAX_PF4D2_SX;
      }else if (deviceName.equals("WAX-PF4D3-SX")){
         return WAX_PF4D3_SX;
      }else if (deviceName.equals("WAX_PF4D4_SX")){
         return WAX_PF4D4_SX;
      }else if (deviceName.equals("WAX-PF3H1-SX")) {
         return WAX_PF3H1_SX;
      } else if (deviceName.equals("WAX-PF3D1-SX")){
         return WAX_PF3D1_SX;
      } else if (deviceName.equals("USB3.0")){
         return WAX_FACE_USB3;
      }else if (deviceName.equals("USB 2.0 Camera")){
         SpecialUsbTag.setOldUsbFace(true);
         return WAX_FACE_USB_OLD;
      }else if (deviceName.equals("WAX-PF3F2-SX")){
         return WAX_PF3F2_SX;
      } else if (deviceName.equals("USB Camera")){
         return WAX_DETECT_FACE;
      } else {
         return WAX_DEFAULT;
      }
//
   }

   public static boolean getDeviceIsDetectHair(String name){
      if (name==null){
         return false;
      }else{
         return WAX_PF3H1_SX.getDeviceName().equals(name);
      }
   }

   public static boolean isNoneMoisture(DeviceConfig deviceConfig){
      return deviceConfig == DeviceConfig.WAX_PF3D1_SX;
   }

}
