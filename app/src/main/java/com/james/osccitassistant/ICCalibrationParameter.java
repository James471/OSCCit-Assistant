package com.james.osccitassistant;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;

import com.google.gson.Gson;

class ICCalibrationParameter {

    private static final String IC_CALIBRATION_PARAMETER = "INTENSITY_CONCENTRATION_CALIBRATION_PARAMETER";

    private final String name;
    private final Rect calibrationRectangle;
    private final int bitmapWidth;
    private final int bitmapHeight;

    public ICCalibrationParameter(String name, Rect rect, int height, int width) {
        this.name = name;
        calibrationRectangle = rect;
        bitmapHeight = height;
        bitmapWidth = width;
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
        return bitmapHeight;
    }

    public static void addICCalibrationParameter(ICCalibrationParameter ICCalibrationParameter, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String parameters = sharedPreferences.getString(IC_CALIBRATION_PARAMETER, "");
        Gson g = new Gson();
        parameters = parameters + g.toJson(ICCalibrationParameter) + ";";
        sharedPreferences.edit().putString(IC_CALIBRATION_PARAMETER, parameters).apply();
    }

    public static void removeICAllCalibrationParameter(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(IC_CALIBRATION_PARAMETER, "").apply();
    }

    public static ICCalibrationParameter[] getAllICCalibrationParameters(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("userPreferences", Context.MODE_PRIVATE);
        String parameters = sharedPreferences.getString(IC_CALIBRATION_PARAMETER, "");
        if (parameters.equals(""))
            return null;
        else {
            String[] parameterArray = parameters.split(";");
            int len = parameterArray.length;
            ICCalibrationParameter[] ICCalibrationParameterArray = new ICCalibrationParameter[len];
            Gson gson = new Gson();
            for (int i = len - 1; i >= 0; i--) {
                ICCalibrationParameterArray[i] = gson.fromJson(parameterArray[i], ICCalibrationParameter.class);
            }
            return ICCalibrationParameterArray;
        }
    }

}
