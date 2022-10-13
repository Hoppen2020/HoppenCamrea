package co.hoppen.cameralib;

/**
 * Created by Administrator on 2018/4/4.
 */

public class UsbInstructionUtils {
    /**
     *  指令：产品码
     * @return
     */
    public static byte[] USB_CAMERA_PRODUCT_CODE() {
        byte [] data = {(byte) 0xAA, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：获取水份值
     * @return
     */
    public static byte[] USB_CAMERA_WATER() {
        byte [] data = {(byte) 0xAA, (byte) 0x10, (byte) 0x02, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }
    /**
     * 指令：获取水份值
     * @return
     */
    public static byte[] USB_CAMERA_WATER_SINGLE_MODE() {
        byte [] data = {0,1};
        return data;
    }

    /**
     * 指令：rgb灯光
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_RGB() {
        byte [] data = {(byte) 0xAA, (byte) 0x10, (byte) 0x03, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }
    /**
     * 指令：偏振灯光
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_POLARIZED() {
        byte [] data = {(byte) 0xAA, (byte) 0x10, (byte) 0x04, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：uv灯光
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_UV() {
        byte [] data =  {(byte) 0xAA, (byte) 0x10, (byte) 0x05, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：关闭灯关
     * @return
     */
    public static byte[] USB_CAMERA_LIGHT_CLOSE() {
        byte [] data =  {(byte) 0xAA, (byte) 0x10, (byte) 0x07, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }

    /**
     * 指令：心跳包
     * @return
     */
    public static byte[] USB_CAMERA_SYS_ONLINE(){
        byte [] data =  {(byte) 0xAA, (byte) 0x10, (byte) 0x01, (byte) 0x00, (byte) 0x00};
        return encryption(data);
    }


    private static byte[] encryption(byte[] data) {
        byte[] returnData = new byte[data.length + 1];
        try {
            byte a = 0;
            for (int i = 0; i < data.length; i++) {
                returnData[i] =data[i];
                if (i!=0){
                    a ^=data[i];
                }
            }
            returnData[data.length] = a;
        } catch (Exception e) {
        }
        return returnData;
    }

}
