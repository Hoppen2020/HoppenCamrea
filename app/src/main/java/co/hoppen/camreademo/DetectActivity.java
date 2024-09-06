package co.hoppen.camreademo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.hoppen.uvc.IButtonCallback;
import com.hoppen.uvc.UVCCamera;

import java.nio.ByteBuffer;
import java.util.Arrays;

import co.hoppen.algorithm.AlgorithmUtils;
import co.hoppen.algorithm.ImageResult;
import co.hoppen.cameralib.CallBack.CaptureCallback;
import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.CallBack.OnFrameListener;
import co.hoppen.cameralib.HoppenCamera;
import co.hoppen.cameralib.HoppenController;

/**
 * Created by YangJianHui on 2024/5/2.
 */
public class DetectActivity extends BaseActivity implements IButtonCallback, View.OnLongClickListener {

    private HoppenController controller;

    private AppCompatImageView capture,filter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);
        initCamera();
        capture = findViewById(R.id.capture);
        filter = findViewById(R.id.filter);
        capture.setOnLongClickListener(this);
        filter.setOnLongClickListener(this);
    }

    public void onCapture(View view){
        view.setVisibility(View.GONE);
        filter.setVisibility(View.VISIBLE);
    }

    public void onFilter(View view){
        view.setVisibility(View.GONE);
        capture.setVisibility(View.VISIBLE);
    }

    private void initCamera() {
        controller = new HoppenCamera.Builder(findViewById(R.id.camera))
                .setCameraButtonListener(this)
                .setFrameFormat(UVCCamera.FRAME_FORMAT_MJPEG)
                .build();
    }

    @Override
    public void onButton(int button, int state) {
        if (button==1){
            controller.capturePicture(new CaptureCallback() {
                @Override
                public void onCapture(Bitmap bitmap) {
                    capture.setVisibility(View.VISIBLE);
                    filter.setVisibility(View.GONE);
                    capture.setImageBitmap(bitmap);
                    ImageResult imageResult = AlgorithmUtils.get(2, bitmap, 0);
                    filter.setImageBitmap(imageResult.bitmap);
                }
            });
        }
    }

    @Override
    public boolean onLongClick(View view) {
        filter.setVisibility(View.GONE);
        capture.setVisibility(View.GONE);
        return true;
    }
}
