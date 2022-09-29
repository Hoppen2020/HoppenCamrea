package co.hoppen.camreademo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by YangJianHui on 2022/9/7.
 */
public class NullActivity extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_null);
   }

   public void back(View view){
      startActivity(new Intent(this,FaceActivity.class));
      finish();
   }

}
