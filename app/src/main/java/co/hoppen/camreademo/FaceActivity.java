package co.hoppen.camreademo;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

//import com.blankj.utilcode.util.ImageUtils;
//import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.hoppen.cameralib.HoppenCamera;
import co.hoppen.cameralib.HoppenController;
import co.hoppen.cameralib.CallBack.OnDeviceListener;
import co.hoppen.cameralib.widget.UVCCameraTextureView;

/**
 * Created by YangJianHui on 2022/2/24.
 */
public class FaceActivity extends BaseActivity implements OnDeviceListener, View.OnLongClickListener {
   private ImageView faceView;
   private UVCCameraTextureView textureView;
   private HoppenController hoppenController;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_face);
      initView();
   }

   @Override
   protected void onResume() {
      super.onResume();
      hoppenController.startPreview();
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
      hoppenController = new HoppenCamera.Builder(textureView).build();
      faceView.setOnLongClickListener(this);
   }

   @Override
   public void onConnected(String productName) {

   }

   @Override
   public void onDisconnect() {

   }

   public void rgb(View view){
      hoppenController.rgbLight();
   }

   public void uv(View view){
      hoppenController.uvLight();
   }

   public void polarized(View view){
      hoppenController.polarizedLight();
   }

   public void balanced(View view){
      hoppenController.balancedPolarizedLight();
   }

   public void wood(View view){
      hoppenController.woodLight();
   }

   public void close(View view){
      hoppenController.closeLight();
   }

   public void t(View view){
      //hoppenController.closeDevices();
      startActivity(new Intent(this,NullActivity.class));
      finish();
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
//      LogUtils.e(bitmap.getWidth(),bitmap.getHeight());
//
      //Bitmap rotate = ImageUtils.rotate(bitmap, 90, bitmap.getWidth() / 2, bitmap.getWidth() / 2);
//      save(rotate);
      return true;
   }

   public void es(View view){
      WaxSystemUtlis.fileManager(this);
   }

   private void save(Bitmap bitmap){
      try {
         File parentFile = new File(Environment.getExternalStorageDirectory().getPath()+"/test2");
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
