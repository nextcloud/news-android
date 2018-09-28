package de.luhmer.owncloudnewsreader.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

public class SquareRoundedBitmapDisplayer implements BitmapDisplayer {

    protected final int cornerRadius;
    protected final int margin;

    public SquareRoundedBitmapDisplayer(int cornerRadiusPixels) {
        this(cornerRadiusPixels, 0);
    }

    public SquareRoundedBitmapDisplayer(int cornerRadiusPixels, int marginPixels) {
        this.cornerRadius = cornerRadiusPixels;
        this.margin = marginPixels;
    }

    @Override
    public void display(Bitmap bitmap, ImageAware imageAware, LoadedFrom loadedFrom) {
        if (!(imageAware instanceof ImageViewAware)) {
            throw new IllegalArgumentException("ImageAware should wrap ImageView. ImageViewAware is expected.");
        }

        //imageAware.setImageDrawable(new RoundedDrawable(bitmap, cornerRadius, margin));
        imageAware.setImageDrawable(new RoundedDrawable(bitmap, cornerRadius));
    }

    public static class RoundedDrawable extends Drawable {

        private Bitmap bitmap;
        private Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private int side;
        private float cornerRadius;
        private RectF dst;

        public RoundedDrawable(Bitmap wrappedBitmap, int cornerRadius) {
            bitmap = wrappedBitmap;
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(16);
            borderPaint.setColor(0xcc220088);
            side = Math.min(bitmap.getWidth(), bitmap.getHeight());
            this.cornerRadius = cornerRadius;
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            Matrix matrix = new Matrix();
            RectF src = new RectF(0, 0, side, side);
            src.offset((bitmap.getWidth() - side) / 2f, (bitmap.getHeight() - side) / 2f);
            dst = new RectF(bounds);
            dst.inset(borderPaint.getStrokeWidth() / 4f, borderPaint.getStrokeWidth() / 4f); // Adjust the factor here to fit into the bounds
            matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

            Shader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            shader.setLocalMatrix(matrix);
            maskPaint.setShader(shader);
            matrix.mapRect(src);
            //radius = src.width() / 2f;
        }

        @Override
        public void draw(Canvas canvas) {
            //Rect b = getBounds();
            //canvas.drawCircle(b.exactCenterX(), b.exactCenterY(), radius, maskPaint);
            //canvas.drawCircle(b.exactCenterX(), b.exactCenterY(), radius + borderPaint.getStrokeWidth() / 2, borderPaint);
            canvas.drawRoundRect(dst, cornerRadius, cornerRadius, maskPaint);
        }

        @Override public void setAlpha(int alpha) {}
        @Override public void setColorFilter(ColorFilter cf) {}
        @Override public int getOpacity() {return PixelFormat.TRANSLUCENT;}

    }
}
