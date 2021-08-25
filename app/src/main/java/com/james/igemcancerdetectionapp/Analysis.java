package com.james.igemcancerdetectionapp;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import androidx.core.util.Pair;

import static android.graphics.Color.red;

class Analysis {

    public static Bitmap getGrayBitmap(Bitmap colored) {
        Bitmap grayBitmap = Bitmap.createBitmap(colored.getWidth(), colored.getHeight(), colored.getConfig());
        float[] matrix = new float[]{
                0.2989f, 0.5870f, 0.114f, 0, 0,
                0.2989f, 0.5870f, 0.114f, 0, 0,
                0.2989f, 0.5870f, 0.114f, 0, 0,
                0, 0, 0, 1, 0,};

        Canvas canvas = new Canvas(grayBitmap);
        Paint paint = new Paint();
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(colored, 0, 0, paint);
        return grayBitmap;
    }

    public static float[] getGrayBitmapConvolution(Bitmap grayBitmap, int delX, int delY) {
        int centre = grayBitmap.getHeight()/2;
        int jLow = centre - (delY/2);
        int jHigh = centre + (delY/2);
        int size = grayBitmap.getWidth()/delX + 1;
        float[] convolution = new float[size];
        for(int i=0, index=0; i<grayBitmap.getWidth(); i+=delX, index++) {
            float temp = 0;
            int count = 0;
            for(int j=jLow; j<jHigh; j++) {
                temp += red(grayBitmap.getPixel(i, j));
                count += 1;
            }
            temp /= count;
            convolution[index] = temp;
        }
        return convolution;
    }

    public static Pair<Integer, Float> getMaxInRange(float[] array, int start, int end) {
        float max = 0;
        int index = 0;
        for(int i=start; i<end; i++) {
            if(array[i]>max) {
                max = array[i];
                index = i;
            }
        }
        return new Pair<>(index, (float)  max);
    }

    public static float[] getXNorm(float x1, float x2, float w1, float w2, int xLen) {
        float[] xNorm = new float[xLen];
        float m = (w2-w1)/(x2-x1);
        for(int i=0; i<xLen; i++) {
            xNorm[i] = m*(i-x1) + w1;
        }
        return xNorm;
    }

}
