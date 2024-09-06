package co.hoppen.algorithm;

import android.graphics.Bitmap;

import java.io.Serializable;

public class ImageResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double score=0;
	public Bitmap bitmap=null;
	public String fazhi;
	public String baifenbi;
	public String pjld;

	public String ratio="0.0%";
}
