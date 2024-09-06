package co.hoppen.algorithm;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageFilter {
	static int boundPix = 135;
	static int wenluPix = 0xff880000;
	public final static int SYPH = 0;// 水油平衡
	public final static int MKQJ = 1;// 毛孔清洁
	public final static int PFMG = 2;// 皮肤敏感
	public final static int SSFJ = 3;// 色素分解
	public final static int JDKS = 4;// 肌底抗衰
	public static float PFMG_fazhi=0.025f;
	public static ImageResult filterImageResult(Bitmap src, int type) {
		ImageResult ir = new ImageResult();
		if (src==null){
			return ir;
		}
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int color, R, G, B;
		int pos, pixColor;
		int gray,vergray;
		float float_R;
		float float_G;
		float float_B;
		int[] pixels = new int[width * height];
		src.getPixels(pixels, 0, width, 0, 0, width, height);
		double count = 0;
		int[] grays = new int[src.getWidth() * src.getHeight()];
		double totalInt=0;
		double percentageTotal=0;
		switch (type){
			case MKQJ:
				for (int y = 0; y < height ; y++) {
					for (int x = 0; x < width ; x++) {
						pos = y * width + x;
						grays[pos] =  Color.red(pixels[pos]);
						totalInt += grays[pos];
					}
				}
				break;
			default:
				for (int y = 0; y < height ; y++) {
					for (int x = 0; x < width; x++) {
						pos = y * width + x;
						pixColor = pixels[pos];
						color = pixColor;
						pixels[pos] = color;
						R = Color.red(color);
						G = Color.green(color);
						B = Color.blue(color);
						grays[pos] = (int) (R * 0.3 + G * 0.59 + B * 0.11);
						totalInt += grays[pos];
					}
				}
				break;
		}
		vergray=(int) (totalInt/grays.length);
		int color_M = 0;
		if (type==SSFJ){
			color_M = get_major_color(src);
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width
					; x++) {
				pos = y * width + x;
				pixColor = pixels[pos];
				color = pixColor;
				pixels[pos] = color;
				// 利用公式计算灰度值
				R = Color.red(color);
				G = Color.green(color);
				B = Color.blue(color);

				float_R = (float) (R) / 765;
				float_G = (float) (G) / 765;
				float_B = (float) (B) / 765;
				switch (type) {
					case MKQJ:
					{
						/**
						 * 滤红色,毛孔清洁
						 */
						//获取该点的亮度，这里取红色分量
						gray = R;
						//滤红点系数
						double ratiored = 1.0;
						if (gray >= 40 && gray <= 80)
							ratiored = 3.0 - (gray - 40) / 20;
						else if (gray < 40) {
							ratiored = 2.5 + (40 - gray) / 40;
						} else {
//							ratiored = 3 - (gray - 80) / 15;
//							ratiored = 2 + (60 - gray) / 40;
//							ratiored = 2.5 + (40 - gray) / 40;
							ratiored = 1.4;

						}

						//滤白点系数
						double ratiowhite = 1.0;
						if (gray >= 40)
							ratiowhite = 2 - (gray - 40) / 172.5;
						else {
							ratiowhite = 2 + (40 - gray) / 50;
						}
						if ((float_R / float_G > ratiored || (float_R / float_B > ratiored)) && gray > vergray * 1.1) {
							//标记红色并计数
//							count++;
//							pixels[pos] = Color.rgb(255, G, B);

							if (!((float_B > 150 || float_G > 150) && Math.abs(float_B - float_G) <= 10))
							{
								count++;
//								bp.SetPixel(i, j, Color.FromArgb(255, Convert.ToInt32(float_G), Convert.ToInt32(float_B)));
								pixels[pos] = Color.rgb(255, (int) float_G, (int) float_B);
								percentageTotal++;
							}

						}
//						if (R - G <= 30 && R - B <= 30 && G - B <= 30
//								&& gray > vergray * ratiowhite) {
//							//标记白色并计数
//							count++;
//							pixels[pos] = Color.rgb(R * 3 < 255 ? R * 3 : 255, G * 3 < 255 ? G * 3 : 255, B * 3 < 255 ? B * 3 : 255);
//						}
					}
					break;
					case SSFJ:
						// 滤黄色,色素分解
//					{
//
//						double Lightper = 255.0 / vergray;
//						double criticalvalue = 1.0/255.0 ;
//						double criticalvalueRG = (Math.sqrt(69.625 * 69.625 + 4 * 11.375 * vergray) + 69.625) / (2 * 11.375);
//						double criticalvalueGR = criticalvalueRG - 10;
//						int RsubG = (Math.abs(Color.red(color_M) - R) - (Math.abs(Color.green(color_M) - G)));
//						int RsubB = (Math.abs(Color.red(color_M) - R) - (Math.abs(Color.blue(color_M) -B)));
//						if ((R - B > 0 || (G - B > 0)) && ((G - R) < criticalvalueGR && (R - G) < criticalvalueRG))// || float_R / float_B > 1.2
//						{
//							if (((Math.abs(Color.red(color_M) - R) > 10 * criticalvalue * Lightper) || (Math.abs(Color.green(color_M) - G) > 15 * Lightper * criticalvalue) && RsubG < 10) ||
//									((Math.abs(Color.red(color_M) - R) > 10 * Lightper * criticalvalue) || (Math.abs(Color.blue(color_M) - B) > 15 * Lightper * criticalvalue) && RsubB < 10))
//							{
//								int RColor = R * 2 <= 255 ? R * 2 : 255;
//								int GColor = G * 2 <= 255 ? G * 2 : 255;
//								pixels[pos]=Color.rgb(RColor, GColor,B);
//								count++;
//							}
//						}
//
//					}
					{
						color = pixels[pos];
						double Lightper = vergray / 255.0;
						double xishudou = 255.0 / 90.0;
						double xishua = -0.4371;
						double xishub = 21.49;
						double xishugenRG = (Math.sqrt(21.49 * 21.49 - 4 * 0.4371 * vergray) + 21.49) / (2 * 0.4371);
						double xishugenGR = xishugenRG - 20;
						int RjianG = (Math.abs(Color.red(color_M) - Color.red(color)) - (Math.abs(Color.green(color_M) - Color.green(color))));
						int RjianB = (Math.abs(Color.red(color_M) - Color.red(color)) - (Math.abs(Color.blue(color_M) - Color.blue(color))));


						if (vergray> 60){//160
							double RchuG = 1;
							if (Color.blue(color) > 0) RchuG = (double) Color.red(color) / (double) Color.blue(color);
							if (RchuG > 1.481 || Color.blue(color) == 0)//1.121
							{
								int RColor = Color.red(color) * 3 <= 255 ? Color.red(color) * 3 : 255;
								int GColor = Color.green(color) * 2 <= 255 ? Color.green(color) * 2 : 255;
//								dstBmp.SetPixel(i, j, Color.FromArgb(RColor, GColor, color1.B));
								pixels[pos] = Color.rgb(RColor, GColor, Color.blue(color));
								count++;
								percentageTotal++;
							}

						}else{

							if ((Color.red(color) - Color.blue(color) > 0 || (Color.green(color) - Color.blue(color) > 0)) && ((Color.green(color) - Color.red(color)) < xishugenGR && (Color.red(color) - Color.green(color)) < xishugenRG))// || float_R / float_B > 1.2
							{
								if (((Math.abs(Color.red(color_M) - Color.red(color)) > 20 * Lightper * xishudou) || (Math.abs(Color.green(color_M) - Color.green(color)) > 15 * Lightper * xishudou) && RjianG < 10) ||
										((Math.abs(Color.red(color_M) - Color.red(color)) > 20 * Lightper * xishudou) || (Math.abs(Color.blue(color_M) - Color.blue(color)) > 15 * Lightper * xishudou) && RjianB < 10)) {
									int RColor = Color.red(color) * 2 <= 255 ? Color.red(color) * 2 : 255;
									int GColor = Color.green(color) * 2 <= 255 ? Color.green(color) * 2 : 255;
									pixels[pos] = Color.rgb(RColor, GColor, Color.blue(color));
									count++;
									percentageTotal++;
								}

//								if ((Math.abs(Color.red(color_M) - Color.red(color)) > 20 * Lightper * xishudou) || (Math.abs(Color.green(color_M) - Color.green(color)) > 15 * Lightper * xishudou) )
//								{
//									int RColor = Color.red(color) * 2 <= 255 ? Color.red(color) * 2 : 255;
//									int GColor = Color.green(color) * 2 <= 255 ? Color.green(color) * 2 : 255;
////									dstBmp.SetPixel(i, j, Color.FromArgb(RColor, GColor, color1.B));
//									pixels[pos] = Color.rgb(RColor, GColor, Color.blue(color));
////									RColor = color1.R * 2 <= 255 ? color1.R * 2 : 255;
////									GColor = color1.G * 2 <= 255 ? color1.G * 2 : 255;
//									count++;
//								}
							}
						}
						if (Color.red(color) <= 130 && Color.blue(color) <= 40)
						{
							int RColor = Color.red(color) * 3 <= 255 ? Color.red(color) * 3 : 255;
							int GColor = Color.green(color) * 2 <= 255 ? Color.green(color) * 2 : 255;
//							dstBmp.SetPixel(i, j, Color.FromArgb(RColor, GColor, color1.B));
							pixels[pos] = Color.rgb(RColor, GColor, Color.blue(color));
							count++;
							percentageTotal++;
						}

					}
					break;
					case PFMG:
						// 滤红色，皮肤敏感
						float sorcepre = (float)170 / (float)vergray;
						vergray = 120;
						int color1 = color;
						gray = (int)((Color.red(color1) * 0.3 + Color.green(color1)* 0.59 + Color.blue(color1) * 0.11) * sorcepre);

						float_R = (float)((Color.red(color1) * sorcepre) > 255 ? 255 : (float)((Color.red(color1) * sorcepre)));
						float_G = (float)((Color.green(color1) * sorcepre) > 255 ? 255 : (float)((Color.green(color1) * sorcepre)));
						float_B = (float)((Color.blue(color1) * sorcepre) > 255 ? 255 : (float)((Color.blue(color1) * sorcepre)));


						double Lightper = (float)255 / vergray;
						double criticalvalueRG = (Math.sqrt(69.625 * 69.625 + 4 * 11.375 * vergray) + 69.625) / (2 * 11.375);
						double criticalvalueRB = criticalvalueRG+1.815;
//						if ((float)R /G > ((vergray * Lightper / 30 / criticalvalueRG)) && ((float)R / B > (vergray * Lightper / 30 / criticalvalueRG)))
//						{
							if (float_R / float_G > ((vergray * Lightper / 30 / criticalvalueRG)) && (float_R / float_B > (vergray * Lightper / 30 / criticalvalueRB))){
								if (float_G / float_B < 1.0 || float_R / float_B < 1.0)//float_R - float_B < 0.0 ||
							{
								count++;
								pixels[pos] = Color.rgb(R*2>255?255:R *2 , 255-G, 255-B);
								percentageTotal++;
//							bp.SetPixel(i, j, Color.FromArgb(255, Convert.ToInt32(float_G), Convert.ToInt32(float_B)));
							}
						}
						if (float_R / float_B >= 2)
						{
							count++;
							//bp.SetPixel(i, j, Color.FromArgb(Convert.ToInt32(float_R * 2) > 255 ? 255 : Convert.ToInt32(float_R * 2), (255 - Convert.ToInt32(float_G)), (255 - Convert.ToInt32(float_B))));
							pixels[pos] = Color.rgb(R*2>255?255:R *2 , 255-G, 255-B);
							percentageTotal++;
						}
						break;
					case 4:
						// 滤深红色
						if (float_R - float_G > 0.030) {
							// bp.SetPixel(i, j, Color.FromArgb(255, 255, 255));
						}
						break;
					case 5:
						// 滤深黄色
						if (float_R - float_B > 0.08) {
							// bp.SetPixel(i, j, Color.FromArgb(0, 0, 0));
						}
						break;
					default:
						break;
				}

			}
		}
		switch (type) {
			case SSFJ:
				if (count <= (float)640 * 480 *1.0/ 20.0)
				{
					ir.score = 70 + 20 * (1.0 - (count * 20 /1.0) / (640 * 480));//70-80
				}
				else if (count > 640 * 480 *1.0 / 20.0 && count <= 640 * 480 * 4.0d /20.0)
				{
					ir.score = 45 + 25 * (1.0 - (count -640 * 480 *1.0/ 20.0) * 20.0/3.0  /(640*480) );//70-80  ***
				}
				else if (count > 640 * 480 *4.0/ 20d && count <= 640 * 480 * 6.0d /20.0)
				{
					ir.score = 25 + 20 * (1.0 - (count -640 * 480 * 5.0 / 20.0) * 10.0 / (640*480));//70-80
				}
				else if (count > 640 * 480 *6.0/ 20d && count <= 640 * 480 *10.0/20.0)
				{
					ir.score = 20 + 5 * (1.0 - (count -640 * 480 * 6.0 / 20.0) * 5.0 /(640*480) );//70-80
				}else{
					ir.score = 20;
				}
				break;
			case MKQJ:
				if (count < (float)640 * 480 / 50d)
				{
					ir.score = 65 + 25 * (1.0 - (count * 50) / (640 * 480));//70-80
				}
				else if (count >= 640 * 480 / 50d && count < 640 * 480 /25.0)//60-70
				{
					ir.score = 50 + 15 * (1.0 - (count - 640*480 / 50) * 50 / (640 * 480));//70-80
				}
				else if (count >= 640 * 480 / 25d && count < 640 * 480 *2.0 /20.0)//50-60
				{
					ir.score = 35 + 15 * (1.0 - (count - 640*480 / 25.0) * 100.0 / 6.0 / (640 * 480));//70-80
				}
				else if (count >= 640 * 480 *2.0d /20d)//30-50
				{
					ir.score = 20 + 15 * (1.0 - (count - 640*480 * 2.0 / 20.0) * 20.0 / 18.0 / (640 * 480));//70-80
				}
				break;
			case PFMG:
				if (count <= (float)640 * 480 / 25.0)
				{
					ir.score = 65 + 25 * (1.0 - (count * 25.0) / (640 * 480));//70-80
					Log.e("@@@@@@@@","1111111");
				}
				else if (count > 640 * 480 / 25.0 && count <= 640 * 480 * 3.0/20)//60-70
				{
					ir.score  =  50 + 15 * (1.0 - ((count - (640 * 480 / 25.0)) * 100.0 /11.0/ (640 * 480)));
					Log.e("@@@@@@@@","22222222");
				}
				else if (count > 640 * 480 *3.0/ 20.0 && count <= 640 * 480 *7.0/ 20.0)//50-60
				{
					ir.score  = 35 + 15 * (1.0 - ((count - (640 * 480 *3.0/20.0)) * 5.0 / (640 * 480)));
					Log.e("@@@@@@@@","33333333");
				}
				else if (count > 640 * 480 * 7.0/20.0&& count <=(640 * 480)/1.0)//30-50
				{
					ir.score = 20 + 15 * (1.0 - ((count - (640 * 480) * 0.7/20.0) * 20.0 / 13.0 / (640 * 480)));
					Log.e("@@@@@@@@","44444444");
				}
				break;
			default:
				if (count < (float)640 * 480 / 40.0)
				{
					ir.score = 70 + 10 * (1.0 - (count * 40.0) / (640 * 480));//70-80
				}
				else if (count > 640 * 480 / 40.0 && count < 640 * 480 / 20.0)//60-70
				{
					ir.score  =  60 + 10 * (1.0 - ((count - (640 * 480 / 40.0)) * 40.0 / (640 * 480)));
				}
				else if (count > 640 * 480 / 20.0 && count < 640 * 480 / 10.0)//50-60
				{
					ir.score  = 50 + 10 * (1.0 - ((count - (640 * 480 / 20.0)) * 20.0 / (640 * 480)));
				}
				else if (count > 640 * 480 / 10.0)//30-50
				{
					ir.score = 40 + 10 * (1.0 - ((count - (640 * 480) * 0.1) * 10.0 / 9.0 / (640 * 480)));
				}
				break;
		}
		if (ir.score < 20) {
			Random random=new Random();
			ir.score = 20+random.nextInt(5);
			Log.e("@@@@@@@@","555555555");
		}
		if (ir.score > 90) {
			Random random=new Random();
			ir.score = 80+random.nextInt(10);
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		ir.bitmap = bitmap;

		ir.pjld=""+vergray;
		ir.fazhi=""+PFMG_fazhi;
		ir.baifenbi=""+(float)count/(width*height);
		ir.ratio =formatDouble((percentageTotal * 100 / pixels.length)) +"%";
		return ir;
	}

	public static ImageResult jdks(Bitmap BP) {
		ImageResult ir = new ImageResult();
		int totalInt = 0;
		int[] grays = new int[BP.getWidth() * BP.getHeight()];
		int R, G, B;
		int pos, pixColor;
		int width = BP.getWidth();
		int height = BP.getHeight();
		int[] pixels = new int[width * height];
		BP.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int y = 0; y < height - 2; y++) {
			for (int x = 0; x < width - 2; x++) {
				pos = y * width + x;
				pixColor = pixels[pos];
				pixels[pos] = pixColor;
				R = Color.red(pixColor);
				G = Color.green(pixColor);
				B = Color.blue(pixColor);
				grays[pos] = (int) (R * 0.3 + G * 0.59 + B * 0.11);
				totalInt += grays[pos];
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(BP.getWidth(), BP.getHeight(),
				Config.ARGB_8888);
		int dHash1 = (BP.getWidth()) * (BP.getHeight());
		int pingjun = totalInt / dHash1;
		int totaldian = 0;
		int shuidiangeshu = 0;
		double dHashstr1 = 0;
		int gray;
		for (int y = 0; y < height - 2; y++) {
			for (int x = 0; x < width - 2; x++) {
				pos = y * width + x;
				pixColor = pixels[pos];
				pixels[pos] = pixColor;
				// 利用公式计算灰度值
				gray = grays[pos];
				if (gray >= pingjun * 1.2 && gray <= 200) {
					totaldian++;
				}
				if (gray >= pingjun * 1.2 && gray > 200) {
					shuidiangeshu++;
				}

				if (gray < pingjun * 1.2) {

				} else if (gray > 90 && gray <= 110) {

				} else if (gray > pingjun) {
					pixels[pos] = Color.rgb(0, 255, 0);
				}
//				pixels[pos]=Color.rgb(gray, gray, gray);
			}
		}
		totaldian = shuidiangeshu * 60 + totaldian * 2;
		dHashstr1 = (double) totaldian / (double) dHash1;

		// ir.score = dHashstr1;

		ir.score = 40 + dHashstr1 * 40;
		if (ir.score > 80) {
			ir.score = 80;
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		ir.bitmap = bitmap;
		return ir;
	}


	public static ImageResult PhotoErzhi_ceshi(Bitmap BP, boolean isOil, float resistance) {
		ImageResult ir = new ImageResult();
		if (BP == null) {
			return ir;
		}
		int totalInt = 0;
		int[] grays = new int[BP.getWidth() * BP.getHeight()];
		int R, G, B;
		int pos, pixColor;
		int width = BP.getWidth();
		int height = BP.getHeight();
		int[] pixels = new int[width * height];
		BP.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = pixels[pos];
				int color = pixColor;
				pixels[pos] = color;
				R = Color.red(color);
				G = Color.green(color);
				B = Color.blue(color);
				grays[pos] = (int) (R * 0.3 + G * 0.59 + B * 0.11);
				totalInt += grays[pos];

			}
		}
		Bitmap bitmap = Bitmap.createBitmap(BP.getWidth(), BP.getHeight(),
				Config.ARGB_8888);
		int dHash1 = (BP.getWidth()) * (BP.getHeight());
		int pingjun = totalInt / dHash1;
		int totaldian = 0;
		int shuidiangeshu = 0;
		double dHashstr1 = 0;
		double percentageTotal= 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = pixels[pos];
				int color = pixColor;
				pixels[pos] = color;
				// 利用公式计算灰度值
				int gray = grays[pos];
				if (gray >= pingjun * 1.2 && gray <= 250) {
					totaldian++;
				}

				if (gray >= pingjun * 1.2 && gray > 250) {
					shuidiangeshu++;
				}

				if (gray < pingjun * 1.2) {

				} else if (gray > 90 && gray <= 110) {

				} else if (gray > 100) {
					pixels[pos] = Color.rgb(0, 255, 0);
					percentageTotal++;
				}
			}
		}

		totaldian = shuidiangeshu * 60 + totaldian * 2;
		dHashstr1 = (double) totaldian / (double) dHash1;
		Log.e("总分","  "+dHashstr1);
		// ir.score = dHashstr1;
		if (isOil){
			ir.score = 40 + (1-dHashstr1) * 40;
		}else{
			ir.score = 40 + dHashstr1 * 40;
		}
		if (ir.score > 80) {
			ir.score = 80;
		}
		if (ir.score<40){
			ir.score =40;
		}
		ir.score = ir.score + new Random().nextInt(5);
		if (resistance!=0&&!isOil){
			if (resistance<=10){
				ir.score =20;
			}else{
				double x =  ((resistance-10)*7/9 +20);
				if (x>=90){
					ir.score =90;
				}else{
					ir.score = x;
				}
			}
			double score = ir.score;
			Log.e("测试 原始",""+score);
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
			Log.e("测试 结果",""+ir.score);
		}


		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		ir.bitmap = bitmap;
		ir.ratio =formatDouble((percentageTotal * 100 / pixels.length)) +"%";
		return ir;
	}

	public static Bitmap drawBitmap(Bitmap bitmap, float[] matrix) {
		Bitmap bt = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bt);
		ColorMatrix cm = new ColorMatrix();
		cm.set(matrix);
		Paint pt = new Paint();
		pt.setColorFilter(new ColorMatrixColorFilter(cm));
		canvas.drawBitmap(bitmap, 0, 0, pt);
		return bt;
	}

	public static Bitmap subLight(Bitmap src) {
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int dst[] = new int[width * height];
		src.getPixels(dst, 0, width, 0, 0, width, height);
		int R, G, B;
		int pos, pixColor;
		int addPix = 10;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				R = Color.red(pixColor);
				G = Color.green(pixColor);
				B = Color.blue(pixColor);
				R -= addPix;
				G -= addPix;
				B -= addPix;
				if (R < 0)
					R = 0;
				if (G < 0)
					G = 0;
				if (B < 0)
					B = 0;

				dst[pos] = Color.rgb(R, G, B);
			}
		}
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return bitmap;

	}

	public static Bitmap addLight(Bitmap src) {
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int dst[] = new int[width * height];
		src.getPixels(dst, 0, width, 0, 0, width, height);
		int R, G, B;
		int radx = width / 2;
		int rady = height;
		double rad;
		int pos, pixColor;
		int basicPix2 = 50;
		int addPix;

		double maxRad = Math.sqrt(Math.pow(radx, 2) + Math.pow(rady, 2));
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				R = Color.red(pixColor);
				G = Color.green(pixColor);
				B = Color.blue(pixColor);
				rad = Math.sqrt(Math.pow((x - radx), 2)
						+ Math.pow((y - rady), 2));
				if ((R * 0.299 + G * 0.587 + B * 0.114) > 115) {
					continue;
				}
				addPix = (int) (basicPix2 * rad / maxRad);
				R += addPix;
				G += addPix;
				B += addPix;
				if (R > 255)
					R = 255;
				if (G > 255)
					G = 255;
				if (B > 255)
					B = 255;
				dst[pos] = Color.rgb(R, G, B);
			}
		}
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return bitmap;

	}

	public static Bitmap findColor(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int dst[] = new int[width * height];
		bmp.getPixels(dst, 0, width, 0, 0, width, height);
		int R;
		int pos, pixColor;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				R = Color.red(pixColor);
				if (R > boundPix) {
					pixColor = wenluPix;

				} else {
					pixColor = 0x00000000;
				}
				dst[pos] = pixColor;
			}
		}
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return bitmap;
	}

	public static Bitmap detecedAgain(Bitmap bmp, Bitmap src) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int dst[] = new int[width * height];
		bmp.getPixels(dst, 0, width, 0, 0, width, height);
		int pos, pixColor;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				Color.red(pixColor);
				Color.green(pixColor);
				Color.blue(pixColor);
				dst[pos] = pixColor;
			}
		}
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return bitmap;

	}

//	public static Bitmap DissolveFilter(Bitmap bmp) {
//		int width = bmp.getWidth();
//		int height = bmp.getHeight();
//		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//		int dst[] = new int[width * height];
//		bmp.getPixels(dst, 0, width, 0, 0, width, height);
//		int pos, pixColor;
//		float density = 1;
//		float softness = 0;
//		float minDensity, maxDensity;
//		float d = (1 - density) * (1 + softness);
//		minDensity = d - softness;
//		maxDensity = d;
//		Random randomNumbers = new Random(0);
//		for (int y = 0; y < height; y++) {
//			for (int x = 0; x < width; x++) {
//				pos = y * width + x;
//				pixColor = dst[pos];
//				Color.red(pixColor);
//				Color.green(pixColor);
//				Color.blue(pixColor);
//				int a = (pixColor >> 24) & 0xff;
//				float v = randomNumbers.nextFloat();
//				float f = ImageMath.smoothStep(minDensity, maxDensity, v);
//
//				dst[pos] = ((int) (a * f) << 24) | pixColor & 0x00ffffff;
//			}
//		}
//		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
//		return bitmap;
//
//	}

	public static Bitmap MoltenFilter(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int dst[] = new int[width * height];
		bmp.getPixels(dst, 0, width, 0, 0, width, height);
		int R, G, B, pixel;
		int pos, pixColor;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				R = Color.red(pixColor);
				G = Color.green(pixColor);
				B = Color.blue(pixColor);
				pixel = R * 128 / (G + B + 1);
				if (pixel < 0)
					pixel = 0;
				if (pixel > 255)
					pixel = 255;
				R = pixel;

				pixel = G * 128 / (B + R + 1);
				if (pixel < 0)
					pixel = 0;
				if (pixel > 255)
					pixel = 255;
				G = pixel;

				pixel = B * 128 / (R + G + 1);
				if (pixel < 0)
					pixel = 0;
				if (pixel > 255)
					pixel = 255;
				B = pixel;

				dst[pos] = Color.rgb(R, G, B);
			}
		}
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return bitmap;

	}

	public static Bitmap InvertFilter(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int dst[] = new int[width * height];
		bmp.getPixels(dst, 0, width, 0, 0, width, height);
		int pos;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				dst[pos] = 0XFF000000 | (~dst[pos] & 0x00ffffff);
			}
		}
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return bitmap;

	}

	public static Bitmap grayFilter(Bitmap bmp) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int dst[] = new int[width * height];
		bmp.getPixels(dst, 0, width, 0, 0, width, height);
		int R, G, B;
		int pos, pixColor;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				R = Color.red(pixColor);
				G = Color.green(pixColor);
				B = Color.blue(pixColor);
				R = R * 77;
				G = G * 151;
				B = B * 28;
				int RGB = (R + G + B) >> 8;
				dst[pos] = 0XFF000000 | (RGB << 16) | (RGB << 8) | RGB;
			}
		}
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);

		return clipBitmap(bitmap, bmp);

	}

	public static Bitmap clipBitmap(Bitmap src, Bitmap bmp) {
		Bitmap result = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		Rect rect = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
		canvas.drawBitmap(bmp, rect, rect, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(src, rect, rect, paint);
		return result;
	}

	public static Bitmap iceFilter(Bitmap bmp) {

		int width = bmp.getWidth();
		int height = bmp.getHeight();

		int dst[] = new int[width * height];
		bmp.getPixels(dst, 0, width, 0, 0, width, height);

		int R, G, B, pixel;
		int pos, pixColor;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];

				R = Color.red(pixColor);
				G = Color.green(pixColor);
				B = Color.blue(pixColor);

				pixel = R - G - B;
				pixel = pixel * 3 / 2;
				if (pixel < 0)
					pixel = -pixel;
				if (pixel > 255)
					pixel = 255;
				R = pixel;
				pixel = G - B - R;
				pixel = pixel * 3 / 2;
				if (pixel < 0)
					pixel = -pixel;
				if (pixel > 255)
					pixel = 255;
				G = pixel;

				pixel = B - R - G;
				pixel = pixel * 3 / 2;
				if (pixel < 0)
					pixel = -pixel;
				if (pixel > 255)
					pixel = 255;
				B = pixel;
				dst[pos] = Color.rgb(R, G, B);
			}
		}
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		bitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return clipBitmap(bitmap, bmp);
	}
	public static int get_major_color(Bitmap bitmap) {
		// 调的总和
		double sum_hue = 0;
		// 差的阈值
		float threshold = 30;
		int width=bitmap.getWidth();
		int height=bitmap.getHeight();
		int dst[] = new int[ width* height];
		bitmap.getPixels(dst, 0, width, 0, 0, width, height);
		int pos,pixColor;
		// 计算色调总和
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				// sum_hue += hsv[0];
				sum_hue += RGB2HSV(pixColor);
			}
		}

		double avg_hue = sum_hue / (width *height);
		Log.e("avg_hue", "avg_hue"+sum_hue);
		// 差大于阈值的颜色值
		List<Integer> rgbs = new ArrayList<Integer>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pos = y * width + x;
				pixColor = dst[pos];
				double hue = RGB2HSV(pixColor);
				// 如果色差大于阈值，则加入列表
				if (Math.abs(hue - avg_hue) > threshold) {
					rgbs.add(pixColor);
				}
			}
		}
		if (rgbs.size() == 0)
			return Color.BLACK;
		// 计算列表中的颜色均值，结果即为该图片的主色调
		int sum_r = 0, sum_g = 0, sum_b = 0;
		for (int rgb : rgbs) {
			sum_r += Color.red(rgb);
			sum_g += Color.green(rgb);
			sum_b += Color.blue(rgb);
		}

		return Color.rgb(sum_r / rgbs.size(), sum_g / rgbs.size(), sum_b / rgbs.size());
	}

	public static double RGB2HSV(int rgb) {

		int R = Color.red(rgb);
		int G = Color.green(rgb);
		int B = Color.blue(rgb);
		double H = 0;
		int max = Math.max(R, Math.max(G, B));
		int min = Math.min(R, Math.min(G, B));
		if (max == min) {
			H = 0;
		} else {
			if (R == max)
				H = (double)(G - B) / (max - min);
			else if (G == max)
				H = 2 + (double)(B - R) / (max - min);
			else
				H = 4 + (double)(R - G) / (max - min);
		}

		H = H * 60;

		if (H < 0)
			H = H + 360;
		return H;
	}

	private static double formatDouble(double d){
		BigDecimal bg = new BigDecimal(d).setScale(2, RoundingMode.HALF_UP);
		return bg.doubleValue();
	}

	public static Bitmap enhancedContrast(Bitmap bitmap,int contrast){

		int width=bitmap.getWidth();
		int height=bitmap.getHeight();
		int dst[] = new int[ width* height];
		bitmap.getPixels(dst, 0, width, 0, 0, width, height);
		int pos,pixColor;
		// 计算色调总和
		int contrastAvg = 128;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				pos = y * width + x;
				pixColor = dst[pos];
				int R = Color.red(pixColor);
				int G = Color.green(pixColor);
				int B = Color.blue(pixColor);
				if (R<contrastAvg){
					pixColor = R - Math.abs(contrast);
					if (pixColor<0)pixColor = 0;
				}else {
					pixColor = R+Math.abs(contrast);
					if (pixColor>255)pixColor = 255;
				}
				R = pixColor;

				if (G < contrastAvg) {
					pixColor = G- Math.abs(contrast);
					if (pixColor < 0) pixColor = 0;
				} else {
					pixColor = G + Math.abs(contrast);
					if (pixColor > 255) pixColor = 255;
				}
				G = pixColor;
				if (B < contrastAvg)
				{
					pixColor = B- Math.abs(contrast);
					if (pixColor < 0) pixColor = 0;
				} else {
					pixColor = B+ Math.abs(contrast);
					if (pixColor > 255) pixColor = 255;
				}
				B= pixColor;
				dst[pos] = Color.rgb(R,G,B);
			}
		}
		Bitmap newBitmap = Bitmap.createBitmap(width, height,
				Config.ARGB_8888);
		newBitmap.setPixels(dst, 0, width, 0, 0, width, height);
		return newBitmap;
	}


}
