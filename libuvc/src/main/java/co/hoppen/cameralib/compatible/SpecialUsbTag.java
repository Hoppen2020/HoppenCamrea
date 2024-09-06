package co.hoppen.cameralib.compatible;

/**
 * Created by YangJianHui on 2024/4/25.
 */
public class SpecialUsbTag {

   private SpecialUsbTag(){}

   private static SpecialUsbTag instance = new SpecialUsbTag();

//   public static SpecialUsbTag getInstance(){
//      return instance;
//   }
   private static boolean oldUsbFace = false;

   public static void setOldUsbFace(boolean oldUsbFace) {
      SpecialUsbTag.oldUsbFace = oldUsbFace;
   }

   public static boolean isOldUsbFace() {
      return oldUsbFace;
   }
}
