package co.hoppen.cameralib;

import co.hoppen.cameralib.CallBack.OnInfoListener;
import co.hoppen.cameralib.CallBack.OnUsbStatusListener;
import co.hoppen.cameralib.CallBack.OnWaterListener;

/**
 * Created by YangJianHui on 2021/3/16.
 */
public abstract class HoppenDevice implements OnUsbStatusListener {
    protected abstract void sendInstructions(Instruction instruction);
    protected abstract void setOnWaterListener(OnWaterListener onWaterListener);
    protected abstract void setOnInfoListener(OnInfoListener onInfoListener);
    protected abstract void closeDevice();
}
