package co.hoppen.cameralib;

/**
 * Created by YangJianHui on 2022/10/8.
 */
public enum DeviceConfig {
   WAX_04_80("WAX-04+80",640,480,CommunicationType.MCU),
   WAX_PF4D1_MK("WAX-PF4D1-MK",640,480,CommunicationType.MCU),
   WAX_PF4D3_MK("WAX-PF4D3-MK",800,600,CommunicationType.MCU),
   WAX_PF4D2_SX("WAX-PF4D2-SX",640,480,CommunicationType.INTERNAL_THREE_LIGHT),
   WAX_PF4D3_SX("WAX-PF4D3-SX",1280,960,CommunicationType.INTERNAL_THREE_LIGHT),
   WAX_PF4D4_SX("WAX_PF4D4_SX",1280,960,CommunicationType.INTERNAL_THREE_LIGHT),
   WAX_FACE_USB3("USB3.0",1920,1080,CommunicationType.INTERNAL_FIVE_LIGHT),
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
      }else if (deviceName.equals("USB3.0")){
         return WAX_FACE_USB3;
      }else {
         return WAX_DEFAULT;
      }
   }

}
