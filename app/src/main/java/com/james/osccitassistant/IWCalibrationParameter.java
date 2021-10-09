package com.james.osccitassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;

import com.google.gson.Gson;

class IWCalibrationParameter {

    private static final String IW_CALIBRATION_PARAMETER = "INTENSITY_WAVELENGTH_CALIBRATION_PARAMETER";

    private final String name;
    private final Rect calibrationRectangle;
    private final int bitmapWidth;
    private final int bitmapHeight;
    private final float[] calibrationArray;
    private final int delX;
    private final int delY;

    public IWCalibrationParameter(String name, Rect rect, int height, int width, float[] convolution, int delX, int delY) {
        this.name = name;
        calibrationRectangle = rect;
        bitmapHeight = height;
        bitmapWidth = width;
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

    public float[] getCalibrationArray() {
        return calibrationArray;
    }

    public int getDelX() {
        return delX;
    }

    public int getDelY() {
        return delY;
    }

    public static void addIWCalibrationParameter(IWCalibrationParameter IWCalibrationParameter, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String parameters = sharedPreferences.getString(IW_CALIBRATION_PARAMETER, "");
        Gson g = new Gson();
        parameters = parameters + g.toJson(IWCalibrationParameter) + ";";
        sharedPreferences.edit().putString(IW_CALIBRATION_PARAMETER, parameters).apply();
    }

    public static void removeIWAllCalibrationParameter(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(IW_CALIBRATION_PARAMETER, "").apply();
    }

    public static IWCalibrationParameter[] getAllIWCalibrationParameters(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String parameters = sharedPreferences.getString(IW_CALIBRATION_PARAMETER, "");
        if(parameters.equals(""))
            return null;
        else {
            String[] parameterArray = parameters.split(";");
            int len = parameterArray.length;
            IWCalibrationParameter[] IWCalibrationParameterArray = new IWCalibrationParameter[len];
            Gson gson = new Gson();
            for(int i = len-1; i>=0; i--) {
                IWCalibrationParameterArray[i] = gson.fromJson(parameterArray[i], IWCalibrationParameter.class);
            }
            return IWCalibrationParameterArray;
        }
    }

}
