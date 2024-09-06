package co.hoppen.cameralib.CallBack;

import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.Instruction;

/**
 * Created by YangJianHui on 2022/9/28.
 */
public interface ControllerFunction {

    void rgbLight();

    void uvLight();

    void polarizedLight();

    void balancedPolarizedLight();

    void woodLight();

    void closeLight();

    void sendInstruction(Instruction instruction);

    void getMoisture();

    void getMoisture(int tryCount);

    void getProductCode();

    void getUniqueCode();

    void startPreview();

    void stopPreview();

    void closeDevices();

    void capturePicture(CaptureCallback captureCallback);

    void capturePicture(int width, int height, CaptureCallback captureCallback);

    void captureViewPicture(CaptureCallback captureCallback);

    void captureViewPicture(int width, int height, CaptureCallback captureCallback);
}
