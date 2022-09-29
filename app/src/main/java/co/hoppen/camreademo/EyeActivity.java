package co.hoppen.camreademo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import co.hoppen.cameralib.ErrorCode;
import co.hoppen.cameralib.HoppenCameraHelper;
import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.CallBack.OnButtonListener;
import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2022/2/26.
 */
public class EyeActivity extends BaseActivity implements OnDeviceListener, OnButtonListener {
   private HoppenController controller;
   private UVCCameraTextureView camera;
   private ImageView image;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_eye);
      camera = findViewById(R.id.tv_camera);
      image = findViewById(R.id.iv_image);
      controller = HoppenCameraHelper.createController(this,camera,this);
      controller.setDeviceButton(this);
   }

   @Override
   public void onConnected() {
      Toast.makeText(this,"已连接",Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onDisconnect(ErrorCode errorCode) {
      Toast.makeText(this,"已断开",Toast.LENGTH_SHORT).show();
   }

   @Override
   protected void onPause() {
      super.onPause();
   }

   @Override
   public void onButton(int state) {
      Log.e("@@",""+state);
      if (state==1){
            if (image.getVisibility()== View.VISIBLE){
               image.setVisibility(View.GONE);
            }else {
               Bitmap bitmap = camera.getBitmap();
               image.setVisibility(View.VISIBLE);
               image.setImageBitmap(bitmap);
            }
      }
   }
}
