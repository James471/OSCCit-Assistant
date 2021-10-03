package com.james.igemcancerdetectionapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;

import com.google.gson.Gson;

class CalibrationParameter {

    private final String name;
    private final Rect calibrationRectangle;
    private final int bitmapWidth;
    private final int bitmapHeight;
    private final float rotationAngle;
    private final float[] calibrationArray;
    private final int delX;
    private final int delY;

    public CalibrationParameter(String name, Rect rect, int height, int width, float[] convolution, int delX, int delY) {
        this.name = name;
        calibrationRectangle = rect;
        bitmapHeight = height;
        bitmapWidth = width;
        rotationAngle = 0.0f;
        calibrationArray = convolution;
        this.delX = delX;
        this.delY = delY;
    }

    public CalibrationParameter(String name, Rect rect, Float angle, int height, int width, float[] convolution, int delX, int delY) {
        this.name = name;
        calibrationRectangle = rect;
        bitmapHeight = height;
        bitmapWidth = width;
        rotationAngle = angle;
        calibrationArray = convolution;
        this.delX = delX;
        this.delY = delY;
    }

    public String getName() {
        return name;
    }

    public Rect getCalibrationRectangle() {
        return calibrationRectangle;
    }

    public int getBitmapWidth() {
        return bitmapWidth;
    }

    public int getBitmapHeight() {
        return  bitmapHeight;
    }

    public float getRotationAngle() {
        return rotationAngle;
    }

    public float[] getCalibrationArray() {
        return calibrationArray;
    }

    public int getDelX() {
        return delX;
    }

    public int getDelY() {
        return delY;
    }

    public static void addCalibrationParameter(CalibrationParameter calibrationParameter, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String parameters = sharedPreferences.getString("calibrationParams", "");
        Gson g = new Gson();
        parameters = parameters + g.toJson(calibrationParameter) + ";";
        sharedPreferences.edit().putString("calibrationParams", parameters).apply();
    }

    public static void removeAllCalibrationParameter(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("calibrationParams", "").apply();
    }

    public static CalibrationParameter[] getAllCalibrationParameters(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String parameters = sharedPreferences.getString("calibrationParams", "");
        if(parameters.equals(""))
            return null;
        else {
            String[] parameterArray = parameters.split(";");
            int len = parameterArray.length;
            CalibrationParameter[] calibrationParameterArray = new CalibrationParameter[len];
            Gson gson = new Gson();
            for(int i = len-1; i>=0; i--) {
                calibrationParameterArray[i] = gson.fromJson(parameterArray[i], CalibrationParameter.class);
            }
            return calibrationParameterArray;
        }
    }

}
