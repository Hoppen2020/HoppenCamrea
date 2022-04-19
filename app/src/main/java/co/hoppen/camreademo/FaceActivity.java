package co.hoppen.camreademo;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import co.hoppen.cameralib.DeviceFilter;
import co.hoppen.cameralib.ErrorCode;
import co.hoppen.cameralib.HoppenCameraHelper;
import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.Instruction;
import co.hoppen.cameralib.OnDeviceListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2022/2/24.
 */
public class FaceActivity extends BaseActivity implements OnDeviceListener, View.OnLongClickListener {
   private HoppenController hoppenController;
   private ImageView faceView;
   private UVCCameraTextureView textureView;


   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_face);
      initView();
   }

   private void initView() {
        textureView = findViewById(R.id.tv_camera);
        textureView.setBitmapSize(1920,1080);
        faceView = findViewById(R.id.iv_face);
//      Matrix matrix = new Matrix();
//      DisplayMetrics dm = getResources().getDisplayMetrics();
//      int w_screen = dm.widthPixels;
//      int h_screen = dm.heightPixels;
//      matrix.postScale(-1, 1, w_screen/2, h_screen / 2);//镜像水平翻转
//      textureView.setTransform(matrix);
      hoppenController = HoppenCameraHelper.createController(this,textureView,this);

      faceView.setOnLongClickListener(this);

   }

   @Override
   public void onConnected() {

   }

   @Override
   public void onDisconnect(ErrorCode errorCode) {

   }

   @Override
   protected void onStop() {
      super.onStop();
      if (hoppenController!=null)hoppenController.close();
   }

   public void rgb(View view){
      if (hoppenController!=null)hoppenController.sendInstructions(Instruction.LIGHT_RGB);
   }

   public void uv(View view){
      if (hoppenController!=null)hoppenController.sendInstructions(Instruction.LIGHT_UV);
   }

   public void polarized(View view){
      if (hoppenController!=null)hoppenController.sendInstructions(Instruction.LIGHT_POLARIZED);
   }

   public void balanced(View view){
      if (hoppenController!=null)hoppenController.sendInstructions(Instruction.LIGHT_BALANCED_POLARIZED);
   }

   public void wood(View view){
      if (hoppenController!=null)hoppenController.sendInstructions(Instruction.LIGHT_WOOD);
   }

   public void close(View view){
      if (hoppenController!=null)hoppenController.sendInstructions(Instruction.LIGHT_CLOSE);
   }

   private Bitmap getImageFromAssetsFile(String fileName) {
      Bitmap image = null;
      AssetManager am = getResources().getAssets();
      try {
         InputStream is = am.open(fileName);
         image = BitmapFactory.decodeStream(is);
         is.close();
      }catch (IOException e) {
         e.printStackTrace();
      }
      return image;
   }

   @Override
   public boolean onLongClick(View v) {
      Bitmap bitmap = textureView.getBitmap(1920,1080);
//      faceView.setImageBitmap(bitmap);
      LogUtils.e(bitmap.getWidth(),bitmap.getHeight());

      Bitmap rotate = ImageUtils.rotate(bitmap, 90, bitmap.getWidth() / 2, bitmap.getWidth() / 2);

      save(rotate);
      return true;
   }

   public void es(View view){
      WaxSystemUtlis.fileManager(this);
   }

   private void save(Bitmap bitmap){
      try {
         File parentFile = new File(Environment.getExternalStorageDirectory().getPath()+"/test");
         if (!parentFile.exists()) {
            parentFile.mkdirs();
         }
         String name = String.valueOf(System.currentTimeMillis()) + ".jpg";
         File file = new File(parentFile.getPath(), name);
         FileOutputStream outputStream = new FileOutputStream(file);
         bitmap.compress(Bitmap.CompressFormat.JPEG,
                 100, outputStream);
         outputStream.flush();
         outputStream.close();
      }catch (Exception e){
      }
      Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show();
   }

}
