package co.hoppen.camreademo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.jiangdg.uvc.IButtonCallback;
import com.jiangdg.uvc.UVCCamera;

import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.CallBack.OnInfoListener;
import co.hoppen.cameralib.CallBack.OnMoistureListener;
import co.hoppen.cameralib.HoppenCamera;
import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.Instruction;

/**
 * Created by YangJianHui on 2022/9/27.
 */
public class NewDeviceActivity extends AppCompatActivity implements OnMoistureListener, IButtonCallback, OnDeviceListener, OnInfoListener {
   private HoppenController controller;


   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_new_device);
      controller = new HoppenCamera.Builder(findViewById(R.id.tv_camera))
              .setOnMoistureListener(this)
              .setCameraButtonListener(this)
              .setOnDeviceListener(this)
              .setOnInfoListener(this)
              .setFrameFormat(UVCCamera.FRAME_FORMAT_MJPEG)
              .build();
   }

   public void onClick(View view){
      switch (view.getId()){
         case R.id.btn_polarized:
            controller.polarizedLight();
            break;
         case R.id.btn_uv:
            controller.uvLight();
            break;
         case R.id.btn_rgb:
            controller.rgbLight();
            break;
         case R.id.btn_close:
            controller.closeLight();
            break;
         case R.id.btn_water:
            controller.getMoisture();
            break;
         case R.id.btn_id:
            controller.getUniqueCode();
            break;
         case R.id.btn_start:
            controller.startPreview();
            break;
         case R.id.btn_stop:
            controller.stopPreview();
            break;
         case R.id.iv_capture:
            view.setVisibility(View.GONE);
            break;
      }

   }

   @Override
   public void onMoistureCallBack(float value) {
      Log.e("onMoistureCallBack",""+value);
      ((TextView)findViewById(R.id.tv_water)).setText(""+value);
   }

   @Override
   public void onButton(int button, int state) {
      Log.e("onButton",button+ "  "+state);
      if (state==1){
         controller.capturePicture(new HoppenController.CaptureResult() {
            @Override
            public void onCapture(Bitmap bitmap) {
               ImageView captureView = findViewById(R.id.iv_capture);
               captureView.setVisibility(View.VISIBLE);
               captureView.setImageBitmap(bitmap);
            }
         });
      }
   }

   @Override
   public void onConnected(String productName) {
      ((TextView)findViewById(R.id.tv_status)).setText("已连接");
      findViewById(R.id.fl_loading).setVisibility(View.GONE);
      ((TextView)findViewById(R.id.tv_device)).setText(productName+"");
   }

   @Override
   public void onDisconnect() {
      ((TextView)findViewById(R.id.tv_status)).setText("已断开");
      findViewById(R.id.fl_loading).setVisibility(View.VISIBLE);
   }

   @Override
   public void onInfoCallback(Instruction instruction, String info) {
      Log.e("onInfoCallback",""+info);
      ((TextView)findViewById(R.id.tv_id)).setText(info);
   }

}
