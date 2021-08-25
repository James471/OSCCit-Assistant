package com.james.igemcancerdetectionapp;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class CommenceAnalysisActivity extends AppCompatActivity implements CropImageView.OnCropImageCompleteListener, CropImageView.OnSetImageUriCompleteListener, AdapterView.OnItemSelectedListener {

    CropImageView cropImageView;
    Uri fullImageUri;
    Bitmap croppedImage;
    CalibrationParameter chosenParameter;
    CalibrationParameter[] parameters;

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
        setContentView(R.layout.activity_commence_analysis);
        cropImageView = findViewById(R.id.analysisCropImageView);
        cropImageView.setOnCropImageCompleteListener(this);
        cropImageView.setOnSetImageUriCompleteListener(this);

        findViewById(R.id.btnGetImgFromCamera).setOnClickListener(view -> Toast.makeText(getApplicationContext(), "This doesn't do anything", Toast.LENGTH_LONG).show());

        findViewById(R.id.btnGetImgFromGallery).setOnClickListener(view -> checkAndRequestStoragePermissions());

        findViewById(R.id.chooseCalibParam).setOnClickListener(view -> showChooseCalibrationParameterDialog());

        findViewById(R.id.showResultsBtn).setOnClickListener(view -> cropImageView.getCroppedImageAsync());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        chosenParameter = parameters[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    protected void checkAndRequestStoragePermissions() {

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
            chooseImage.setType("image/*");
            chooseImage = Intent.createChooser(chooseImage, "Pick an image");
            imagePickLauncher.launch(chooseImage);
        }

    }

    protected void showChooseCalibrationParameterDialog() {
        parameters = CalibrationParameter.getAllCalibrationParameters(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dilog_get_calibration_parameter, null);
        Spinner spinner = customLayout.findViewById(R.id.spinnerParamList);
        List<String> spinnerArray = new ArrayList<>();
        if(parameters!=null) {
            for (CalibrationParameter parameter : parameters) {
                spinnerArray.add(parameter.getName());
            }
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);
        Button button = customLayout.findViewById(R.id.btnCreateParam);
        AlertDialog.Builder builder = new AlertDialog.Builder(CommenceAnalysisActivity.this);
        builder.setView(customLayout);
        builder.setPositiveButton("Continue", (dialog, which) -> {
            if(chosenParameter!=null) {
                findViewById(R.id.showResultsBtn).setEnabled(true);
                Rect rect = chosenParameter.getCalibrationRectangle();
                cropImageView.setCropRect(rect);
                Rect rect1 = cropImageView.getCropRect();
                dialog.dismiss();
            }
            else {
                Toast.makeText(getApplicationContext(), "Choose a calibration parameter", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if(chosenParameter==null)
                findViewById(R.id.showResultsBtn).setEnabled(false);
            dialog.dismiss();
        });
        final AlertDialog alertDialog = builder.create();
        button.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(CommenceAnalysisActivity.this, NewCalibrationParameterActivity.class);
            startActivity(intent);
        });
        alertDialog.show();
    }

    protected void analyse() {
        ProgressBar progressBar = findViewById(R.id.analysingProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        Bitmap grayImage = Analysis.getGrayBitmap(croppedImage);
        float[] convolution = Analysis.getGrayBitmapConvolution(grayImage, chosenParameter.getDelX(), chosenParameter.getDelY());
        float[] calibrationArray = chosenParameter.getCalibrationArray();
        int i1 = Analysis.getMaxInRange(calibrationArray, 600, 700).first;
        int i2 = Analysis.getMaxInRange(calibrationArray, 900, 1000).first;
        float[] xNorm = Analysis.getXNorm(i1, i2, 546.5f, 611.6f, calibrationArray.length);
        Intent intent = new Intent(this, AnalysisResultsActivity.class);
        intent.putExtra("xNorm", xNorm);
        intent.putExtra("sampleConvolution", convolution);
        progressBar.setVisibility(View.GONE);
        startActivity(intent);
    }

    @Override
    public void onCropImageComplete(@NonNull CropImageView cropImageView, @NonNull CropImageView.CropResult cropResult) {
        croppedImage = cropResult.getBitmap();
        analyse();
    }

    @Override
    public void onSetImageUriComplete(@NonNull CropImageView cropImageView, @NonNull Uri uri, Exception e) {
        if(e!=null) {
            Toast.makeText(getApplicationContext(), "Failed to get image. Try again!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } else {
            findViewById(R.id.chooseCalibParam).setEnabled(true);
        }
    }
}