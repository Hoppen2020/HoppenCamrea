package co.hoppen.algorithm;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import java.util.Random;

/**
 * Created by Administrator on 2019/1/3.
 */

public class AlgorithmUtils {

    public static ImageResult get(int pos, Bitmap captureBitmap,float resistanceFloat){
        ImageResult ir;
        switch (pos){
            case 0:
                ir=ImageFilter.PhotoErzhi_ceshi(captureBitmap,false,resistanceFloat);
                break;
            case 4:
                ir=ImageFilter.filterImageResult(captureBitmap,ImageFilter.MKQJ);                                                                                                                                                                                                                                                                                                                                                                                               ir=ImageFilter.filterImageResult(captureBitmap,ImageFilter.MKQJ);
                break;
            case 2:
                ir=ImageFilter.filterImageResult(captureBitmap,ImageFilter.PFMG);
                break;
            case 3:
                ir=ImageFilter.filterImageResult(captureBitmap,ImageFilter.SSFJ);
                break;
            default:
                ir=new ImageResult();
                ir.bitmap=clipBitmap(captureBitmap, new InvertFilter());
                ir.ratio ="0.0%";
                if (ir.score > 80) {
                    ir.score = 80;
                }
                if (ir.score<40){
                    ir.score =40;
                }
                if (resistanceFloat!=0){
                    if (resistanceFloat<=10){
                        ir.score =20;
                    }else{
                        double x =  ((resistanceFloat-10)*7/9 +20);
                        if (x>=90){
                            ir.score =90;
                        }else{
                            ir.score = x;
                        }
                    }
                    double score = ir.score;
                    if (score>=20&&score<=29){
                        ir.score=(score-20)  * 25/9 +20;
                    }else if (score>=30&&score<=35){
                        ir.score=(score-30)  * 15/5 +45;
                    }else if (score>=36&&score<=39){
                        ir.score=(score-36)  * 15/3 +50;
                    }else if (score>=40&&score<=75){
                        ir.score=(score-40)  * 10/25 +60;
                    }else if (score>=76&&score<=80){
                        ir.score=(score-76)  * 9/5 +71;
                    }else if (score>=81&&score<=89){
                        ir.score=(score-80)  * 14/9 +75;
                    }else if (score==90){
                        ir.score= new Random().nextInt(10)+80;
                    }
                    if (ir.score<=0){
                        ir.score = new Random().nextInt(10)+40;
                    }
                    if (ir.score>=100){
                        ir.score= new Random().nextInt(10)+80;
                    }

                    if (ir.score<=70){
                        ir.score = ir.score +new Random().nextInt(10)+1;
                    }else {
                        ir.score = 65 + new Random().nextInt(11);
                    }
                }
                break;
        }
        return ir;
    }

    private static Bitmap clipBitmap(Bitmap src, IImageFilter iImageFilter) {
        if (src==null) {
            return null;
        }
        Image image = new Image(src);
        iImageFilter.process(image);
        image.copyPixelsFromBuffer();
        Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        Rect rect=new Rect(0, 0, src.getWidth(), src.getHeight());
        canvas.drawBitmap(src, rect, rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(image.getImage(), rect, rect, paint);
        return bitmap;
    }

}
