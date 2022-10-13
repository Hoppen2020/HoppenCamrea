package co.hoppen.cameralib.CallBack;

/**
 * Created by YangJianHui on 2021/3/15.
 */
public interface OnDeviceListener {

    void onConnected(String productName);

    void onDisconnect();

}
