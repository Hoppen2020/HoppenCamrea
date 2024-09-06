package co.hoppen.cameralib;

/**
 * Created by YangJianHui on 2024/2/19.
 */
public enum DetectType {
    NONE(0),
    SKIN(1),
    HAIR(2),
    EYE(3),
    FUNCTION(4);

    private final int type;

    DetectType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static DetectType getDetectType(int type){
        if (type == 1) {
            return SKIN;
        }else if (type == 2){
            return HAIR;
        }else if (type == 3){
            return EYE;
        }else if (type == 4){
            return FUNCTION;
        }else return NONE;
    }

}
