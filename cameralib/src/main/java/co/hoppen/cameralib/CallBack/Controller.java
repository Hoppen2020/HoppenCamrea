package co.hoppen.cameralib.CallBack;

import android.graphics.SurfaceTexture;

import com.serenegiant.usb.Size;

import java.util.List;

import co.hoppen.cameralib.Instruction;

/**
 * Created by YangJianHui on 2021/3/17.
 */
public interface Controller {
        Size getPreviewSize();

        List<Size> getSupportedPreviewSizes();

        void setSurfaceTexture(SurfaceTexture surfaceTexture);

        void close();

        void setDeviceButton(OnButtonListener onDeviceButton);

        void sendInstructions(Instruction instruction);

        String getDeviceName();

        void setContrast(int contrast);

        void setSaturation(int saturation);

        // void startPreview();

        // void stopPreview();

        // void setPreviewSize(int width,int height);

}
