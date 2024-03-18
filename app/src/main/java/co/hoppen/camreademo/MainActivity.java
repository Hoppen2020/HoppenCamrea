package co.hoppen.camreademo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


/**
 * Created by YangJianHui on 2022/10/11.
 */
public class MainActivity extends AppCompatActivity {


   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
   }

   public void newDevice(View view){
      startActivity(new Intent(this,NewDeviceActivity.class));
      finish();
   }

   public void face(View view){
      startActivity(new Intent(this,FaceActivity.class));
      finish();
   }

}
