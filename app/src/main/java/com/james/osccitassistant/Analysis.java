package com.james.osccitassistant;

import static android.graphics.Color.red;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import androidx.core.util.Pair;
import androidx.exifinterface.media.ExifInterface;

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

    public static float getGrayBitmapAverage(Bitmap grayBitmap) {
        float average = 0;
        int height = grayBitmap.getHeight();
        int width = grayBitmap.getWidth();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                average += red(grayBitmap.getPixel(i, j));
            }
        }
        average /= (height * width);
        return average;
    }

    public static float[] getLinearFitParameters(float[] x, float[] y) {
//        Returns a float array containing m, c, sigM, sigC in that order.
        int N = x.length;
        float sXY = 0.0f;
        float sX = 0.0f;
        float sY = 0.0f;
        float sX2 = 0.0f;
        float sigY = 0.0f;
        float Delta;
        for (int i = 0; i < N; i++) {
            sXY += x[i] * y[i];
            sX += x[i];
            sY += y[i];
            sX2 += x[i] * x[i];
        }
        Delta = N * sX2 - (sX * sX);
        float m = (N * sXY - sX * sY) / Delta;
        float c = (sX2 * sY - sX * sXY) / Delta;
        for (int i = 0; i < N; i++) {
            sigY += (y[i] - m * x[i] - c) * (y[i] - m * x[i] - c);
        }
        sigY = sigY / (N - 2);
        sigY = (float) Math.sqrt(sigY);
        float sigC = (float) (sigY * Math.sqrt(sX2 / Delta));
        float sigM = (float) (sigY * Math.sqrt(N / Delta));
        return new float[]{m, c, sigM, sigC};
    }

    public static float[] linearFunctionInverse(float m, float c, float sigM, float sigC, float y) {
        float x;
        float sigX;
        x = (y - c) / m;
        float temp = (y - c) * sigM / (m * m);
        sigX = (float) Math.sqrt((sigC / m) * (sigC / m) + temp * temp);
        return new float[]{x, sigX};
    }

    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;
        return 0;
    }

    public static float[] getGrayBitmapConvolution(Bitmap grayBitmap, int delX, int delY) {
        int centre = grayBitmap.getHeight() / 2;
        int jLow = centre - (delY / 2);
        int jHigh = centre + (delY / 2);
        int size = grayBitmap.getWidth() / delX + 1;
        float[] convolution = new float[size];
        for (int i = 0, index = 0; i < grayBitmap.getWidth(); i += delX, index++) {
            float temp = 0;
            int count = 0;
            for (int j = jLow; j < jHigh; j++) {
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
        for (int i = start; i < end; i++) {
            if (array[i] > max) {
                max = array[i];
                index = i;
            }
        }
        return new Pair<>(index, max);
    }

    public static float[] getCalibratedArray(int x1, int x2, int x3, float d1, float d2, float d3, int xLen) {
        double a = ((d3 - d1) / (x3 - x1) - (d2 - d1) / (x2 - x1)) / (x3 - x2);
        double b = (d2 - d1) / (x2 - x1) - a * (x2 + x1);
        double c = d1 - b * x1 - a * x1 * x1;
        float[] xNorm = new float[xLen];
        for (int i = 0; i < xLen; i++) {
            xNorm[i] = (float) (a * i * i + b * i + c);
        }
        return xNorm;
    }

}
