package de.luhmer.owncloudnewsreader.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

public class SquareRoundedBitmapDisplayer implements BitmapProcessor {

    private final static String TAG = SquareRoundedBitmapDisplayer.class.getCanonicalName();
    protected final int cornerRadius;
    protected final int margin;
    protected final Integer width;


    public SquareRoundedBitmapDisplayer(int cornerRadiusPixels) {
        this(cornerRadiusPixels, 0);
    }

    public SquareRoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
        this(cornerRadiusPixels, marginPixels, null);
    }

    public SquareRoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels, Integer width) {
        this.cornerRadius = cornerRadiusPixels;
        this.margin = marginPixels;
        this.width = width;
    }


    @Override
    public Bitmap process(Bitmap bitmap) {
        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        RectF dst;
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(16);
        borderPaint.setColor(0xcc220088);

        // float scaleFactor = (float) width / bitmap.getWidth();
        int side = Math.min(bitmap.getWidth(), bitmap.getHeight());
        //noinspection SuspiciousNameCombination
        int height = width;

         Log.d(TAG, "scale bitmap " + bitmap.getWidth() + "x" + bitmap.getHeight() + " -> " + width + "x" + height);

        Matrix matrix = new Matrix();
        RectF src = new RectF(0, 0, side, side);
        src.offset((bitmap.getWidth() - side) / 2f, (bitmap.getHeight() - side) / 2f);
        dst = new RectF(0, 0, width, height);
        dst.inset(borderPaint.getStrokeWidth() / 4f, borderPaint.getStrokeWidth() / 4f); // Adjust the factor here to fit into the bounds
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

        Shader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        shader.setLocalMatrix(matrix);
        maskPaint.setShader(shader);
        matrix.mapRect(src);

        Bitmap dstBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstBmp);
        canvas.drawRoundRect(dst, cornerRadius, cornerRadius, maskPaint);
        bitmap.recycle();
        return dstBmp;
    }
}
