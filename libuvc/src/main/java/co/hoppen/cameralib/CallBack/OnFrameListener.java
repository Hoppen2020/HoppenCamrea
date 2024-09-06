package co.hoppen.cameralib.CallBack;

import java.nio.ByteBuffer;

/**
 * Created by YangJianHui on 2024/5/2.
 */
public interface OnFrameListener {
    void onFrame(ByteBuffer byteBuffer);
}
