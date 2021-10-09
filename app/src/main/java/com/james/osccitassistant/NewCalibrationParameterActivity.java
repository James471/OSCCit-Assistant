package com.james.osccitassistant;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.canhub.cropper.CropImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import osccitassistant.R;

public class NewCalibrationParameterActivity extends AppCompatActivity implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {

    CropImageView cropImageView;
    Uri fullImageUri;
    Bitmap croppedImage;
    String calibrationParameterType;

    ActivityResultLauncher<Intent> imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
        if(activityResult.getResultCode() == Activity.RESULT_OK) {
            Intent resultData = activityResult.getData();
            if (resultData != null) {
                fullImageUri = resultData.getData();
                cropImageView.setImageUriAsync(fullImageUri);
            }
        }
    });

    ActivityResultLauncher<String> requestStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if(isGranted) {
            Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
            chooseImage.setType("image/*");
            chooseImage = Intent.createChooser(chooseImage, "Pick an image");
            imagePickLauncher.launch(chooseImage);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Storage Permission Needs To Be Given", Toast.LENGTH_SHORT);
            toast.show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_calibration_parameter);

        Intent intent = getIntent();
        calibrationParameterType = intent.getStringExtra("type");

        cropImageView = findViewById(R.id.calibrationCropImageView);
        cropImageView.setOnSetImageUriCompleteListener(this);
        cropImageView.setOnCropImageCompleteListener(this);

        Button btnSaveParam = findViewById(R.id.btnSaveParam);
        btnSaveParam.setOnClickListener(v -> cropImageView.getCroppedImageAsync());

        FloatingActionButton floatingActionButtonGallery = findViewById(R.id.FABGallery);
        floatingActionButtonGallery.setOnClickListener(v -> {
            RelativeLayout FABRL = findViewById(R.id.cameraGalleryFABLayout);
            FABRL.setVisibility(View.GONE);
            LinearLayout cropViewLayout = findViewById(R.id.cropViewLayout);
            cropViewLayout.setVisibility(View.VISIBLE);
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
                chooseImage.setType("image/*");
                chooseImage = Intent.createChooser(chooseImage, "Pick an image");
                imagePickLauncher.launch(chooseImage);
            }
        });

    }

    @Override
    public void onCropImageComplete(@NonNull CropImageView cropImageView, @NonNull CropImageView.CropResult cropResult) {
        croppedImage = cropResult.getBitmap();
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_get_name, null);
        new AlertDialog.Builder(this)
                .setView(customLayout)
                .setPositiveButton("Save", (dialog, which) -> {
                    EditText editText = customLayout.findViewById(R.id.edtTxtParameterName);
                    String parameterName = editText.getText().toString();
                    if(!parameterName.isEmpty()) {
                        if(calibrationParameterType.equals("IW"))
                            storeIWCalibrationParameter(parameterName, cropResult);
                        else if(calibrationParameterType.equals("IC"))
                            storeICCalibrationParameter(parameterName, cropResult);
                    } else {
                        Toast.makeText(getApplicationContext(), "Enter a parameter name", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Toast.makeText(getApplicationContext(), "The parameter was not saved", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void onSetImageUriComplete(@NonNull CropImageView cropImageView, @NonNull Uri uri, Exception e) {
        if(e!=null) {
            Toast.makeText(getApplicationContext(), "Failed to get the image. Try again!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } else {
            findViewById(R.id.btnSaveParam).setEnabled(true);
        }
    }

    protected void storeIWCalibrationParameter(String parameterName, CropImageView.CropResult cropResult) {
        ProgressBar progressBar = findViewById(R.id.saveCalibParamProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        Bitmap grayImage = Analysis.getGrayBitmap(croppedImage);
        int delX = 1;
        int delY = 50;
        float[] convolution = Analysis.getGrayBitmapConvolution(grayImage, delX, delY);
        int originalWidth = Objects.requireNonNull(cropResult.getWholeImageRect()).right;
        int originalHeight = cropResult.getWholeImageRect().bottom;
        Rect calibrationRectangle = cropResult.getCropRect();
        IWCalibrationParameter iwCalibrationParameter = new IWCalibrationParameter(parameterName, calibrationRectangle, originalHeight, originalWidth, convolution, delX, delY);
        IWCalibrationParameter.addIWCalibrationParameter(iwCalibrationParameter, this);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "Calibration Parameter saved.", Toast.LENGTH_LONG).show();
        finish();
    }

    protected void storeICCalibrationParameter(String parameterName, CropImageView.CropResult cropResult) {
        ProgressBar progressBar = findViewById(R.id.saveCalibParamProgressBar);
        progressBar.setVisibility((View.VISIBLE));
        int originalWidth = Objects.requireNonNull(cropResult.getWholeImageRect()).right;
        int originalHeight = cropResult.getWholeImageRect().bottom;
        Rect calibrationRectangle = cropResult.getCropRect();
        ICCalibrationParameter icCalibrationParameter = new ICCalibrationParameter(parameterName, calibrationRectangle, originalHeight, originalWidth);
        ICCalibrationParameter.addICCalibrationParameter(icCalibrationParameter, this);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(), "Calibration Parameter saved.", Toast.LENGTH_LONG).show();
        finish();
    }

}