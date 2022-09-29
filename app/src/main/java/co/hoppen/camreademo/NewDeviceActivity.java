package co.hoppen.camreademo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import co.hoppen.cameralib.CameraDevice;
import co.hoppen.cameralib.HoppenCamera;
import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.HoppenController2;

/**
 * Created by YangJianHui on 2022/9/27.
 */
public class NewDeviceActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      HoppenController2 Controller = new HoppenCamera.Builder(this).build();

   }

}
