package co.hoppen.camreademo;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.hoppen.uvc.UVCCamera;

import co.hoppen.cameralib.CameraFilter;
import co.hoppen.cameralib.HoppenCamera;
import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2024/8/13.
 */
public class ZoomActivity extends BaseActivity{

    private HoppenController controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        controller = new HoppenCamera.Builder(findViewById(R.id.camera))
                .setCameraFilter(CameraFilter.ONLY_FUNCTION_CAMERA)
                .setFrameFormat(UVCCamera.FRAME_FORMAT_MJPEG)
                .build();
    }

    public void zoom(View view){
        if (controller==null)return;
        FrameLayout parent = findViewById(R.id.parent);

        ZoomDialog zoomDialog = new ZoomDialog(this, new ZoomDialog.ZoomCameraListener() {
            @Override
            public void onStopPreview() {
                controller.stopPreview();
            }

            @Override
            public void onStartPreview() {
                controller.startPreview();
            }

            @Override
            public void onDialogDismiss(View view) {
                parent.addView(view);
                controller.startPreview();
            }
        });
        zoomDialog.setCanceledOnTouchOutside(true);
        zoomDialog.show();
        View childAt = parent.getChildAt(0);
        parent.removeView(childAt);
        zoomDialog.addView(childAt);
    }

    public void start(View view){
        controller.startPreview();
    }

    public void stop(View view){
        controller.stopPreview();
    }

}
