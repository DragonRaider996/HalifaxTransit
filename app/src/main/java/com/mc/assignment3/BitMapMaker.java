package com.mc.assignment3;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

public class BitMapMaker {
    private final String LOCATION="location";
    //Creating custom bitmap.
    public Bitmap makeBitMap(Context context, int resource, String text) {
        Resources resources = context.getResources();
        Drawable drawable = ContextCompat.getDrawable(context,resource);
        float scale = resources.getDisplayMetrics().density;
        drawable.setBounds(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        if(!text.equals(LOCATION)) {
            Paint paint = new Paint(Paint.DEV_KERN_TEXT_FLAG);
            paint.setColor(Color.BLACK);
            paint.setTextSize(14 * scale);
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int x = 0;
            if (text.length() == 1) {
                x = bitmap.getWidth() - bounds.width() - 25;
            } else if (text.length() == 2) {
                x = bitmap.getWidth() - bounds.width() - 10;
            } else {
                x = bitmap.getWidth() - bounds.width() - 5;
            }

            int y = bounds.height() + 10;
            canvas.drawText(text, x, y, paint);
        }
        return bitmap;
    }

}
