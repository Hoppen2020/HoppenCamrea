package co.hoppen.cameralib;

import android.graphics.SurfaceTexture;

import com.serenegiant.usb.Size;

import java.util.List;

/**
 * Created by YangJianHui on 2021/3/17.
 */
public interface Controller {

        //void setPreviewSize(int width,int height);

        Size getPreviewSize();

        List<Size> getSupportedPreviewSizes();

       // void startPreview();

       // void stopPreview();

        void setSurfaceTexture(SurfaceTexture surfaceTexture);

        void close();

        void setDeviceButton(OnButtonListener onDeviceButton);

        void sendInstructions(Instruction instruction);

        String getDeviceName();
}
