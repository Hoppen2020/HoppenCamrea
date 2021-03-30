package co.hoppen.cameralib;

/**
 * Created by YangJianHui on 2020/3/27.
 */
public enum DeviceType {
    MCU(1),
    CAMERA(2),
    UNKNOWN(-1);

    private int type;

    private DeviceType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static DeviceType getDeviceType(int type){
        if (type==1){
            return MCU;
        }else if (type==2){
            return CAMERA;
        }else {
            return UNKNOWN;
        }
    }
}
