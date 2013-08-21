package de.luhmer.owncloudnewsreader.helper;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ColourCalculator {
	public static int[] averageARGB(Bitmap pic) {
		int A, R, G, B;
		A = R = G = B = 0;
		int pixelColor;
		int width = pic.getWidth();
		int height = pic.getHeight();
		int size = width * height;

		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				pixelColor = pic.getPixel(x, y);
				A += Color.alpha(pixelColor);
				R += Color.red(pixelColor);
				G += Color.green(pixelColor);
				B += Color.blue(pixelColor);
			}
		}

		A /= size;
		R /= size;
		G /= size;
		B /= size;

		int[] average = { A, R, G, B };
		return average;
	}
	
	public static String ColourHexFromBitmap(Bitmap bitmap) {
		int[] colorArr = averageARGB(bitmap);
		int color = Color.argb(colorArr[0], colorArr[1], colorArr[2], colorArr[3]);
		return String.valueOf(color);
	}
}
