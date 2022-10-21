package com.jiangdg.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.blankj.utilcode.util.LogUtils;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by YangJianHui on 2022/10/20.
 */
public class MediaUtils {

   public static Bitmap yuv2Peg(byte [] yuv,int width,int height){
      try {
         YuvImage yuvImage = new YuvImage(yuv, ImageFormat.NV21, width, height, null);

         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(yuv.length);

         boolean result = yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);

         if (!result) return null;

         byte[] data = byteArrayOutputStream.toByteArray();

         return BitmapFactory.decodeByteArray(data, 0, data.length);

      }catch (Exception e){
         LogUtils.e(e.toString());
      }
      return null;
   }


}
