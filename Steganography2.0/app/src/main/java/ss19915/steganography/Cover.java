package ss19915.steganography;

/**
 * Created by ss19915 on 23/12/17.
 */

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;


class Cover{//this class contains all methods and variable neccessary for the image file. Message will be concealed in this image to produce stego image.
	public int height,width,clr,capacity;
	public Bitmap img;
	private int[] pix;
	Cover(Bitmap z){
		try{
			img=z;
			pix =new int[3];
			height=img.getHeight();
			width=img.getWidth();
			capacity=width*(height-1);
		}
		catch(Exception e){
			e.printStackTrace();
			MainActivity.show("Error in reading image");
		}
	}
	public int[] getPixel(int x,int y){
		clr = img.getPixel(x,y);
		pix[0]=Color.red(clr);
		pix[1]=Color.green(clr);
		pix[2]=Color.blue(clr);
		return pix;
	}

}
