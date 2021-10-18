package com.james.osccitassistant;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.core.util.Pair;
import androidx.exifinterface.media.ExifInterface;

import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import osccitassistant.R;

public class CommenceIWAnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ImageView imageView;
    Uri fullImageUri;
    Bitmap fullImage;
    Bitmap croppedImage;
    IWCalibrationParameter chosenParameter;
    IWCalibrationParameter[] parameters;
    ProgressBar progressBar;

    ActivityResultLauncher<Intent> imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
        if (activityResult.getResultCode() == Activity.RESULT_OK) {
            Intent resultData = activityResult.getData();
            if (resultData != null) {
                fullImageUri = resultData.getData();
                try {
                    progressBar.setVisibility(View.VISIBLE);
                    fullImage = getBitmapFromUri(fullImageUri);
                    Glide.with(getApplicationContext())
                            .load(fullImageUri)
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getApplicationContext(), "Failed to load image. Try again.", Toast.LENGTH_LONG).show();
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    findViewById(R.id.chooseCalibParam).setEnabled(true);
                                    findViewById(R.id.showResultsBtn).setEnabled(false);
                                    return false;
                                }
                            })
                            .into(imageView);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    ActivityResultLauncher<String> requestStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
            chooseImage.setType("image/*");
            chooseImage = Intent.createChooser(chooseImage, "Pick an image");
            imagePickLauncher.launch(chooseImage);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Storage Permission Needs To Be Given", Toast.LENGTH_SHORT);
            toast.show();
        }
    });

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commence_iwanalysis);

        imageView = findViewById(R.id.analysisImageView);

        progressBar = findViewById(R.id.analysingProgressBar);

        findViewById(R.id.btnGetImgFromGallery).setOnClickListener(view -> checkAndRequestStoragePermissions());

        findViewById(R.id.chooseCalibParam).setOnClickListener(view -> showChooseCalibrationParameterDialog());

        findViewById(R.id.showResultsBtn).setOnClickListener(view -> analyse());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        chosenParameter = parameters[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    protected void checkAndRequestStoragePermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
            chooseImage.setType("image/*");
            chooseImage = Intent.createChooser(chooseImage, "Pick an image");
            imagePickLauncher.launch(chooseImage);
        }

    }

    protected void showChooseCalibrationParameterDialog() {
        parameters = IWCalibrationParameter.getAllIWCalibrationParameters(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dilog_get_calibration_parameter, null);
        Spinner spinner = customLayout.findViewById(R.id.spinnerParamList);
        List<String> spinnerArray = new ArrayList<>();
        if (parameters != null) {
            for (IWCalibrationParameter parameter : parameters) {
                spinnerArray.add(parameter.getName());
            }
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);
        Button createNewButton = customLayout.findViewById(R.id.btnCreateParam);
        AlertDialog.Builder builder = new AlertDialog.Builder(CommenceIWAnalysisActivity.this);
        builder.setView(customLayout);
        builder.setPositiveButton("Continue", (dialog, which) -> {
            if (chosenParameter != null) {
                progressBar.setVisibility(View.VISIBLE);
                ExifInterface exifInterface;
                try {
                    InputStream inputStream = getContentResolver().openInputStream(fullImageUri);
                    exifInterface = new ExifInterface(inputStream);
                    int rotation = Analysis.exifToDegrees(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL));
                    Rect rect = chosenParameter.getCalibrationRectangle();
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotation);
                    fullImage = Bitmap.createScaledBitmap(fullImage, chosenParameter.getBitmapWidth(), chosenParameter.getBitmapHeight(), true);
                    croppedImage = Bitmap.createBitmap(fullImage, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, matrix, true);
                    Glide.with(getApplicationContext())
                            .load(croppedImage)
                            .addListener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    progressBar.setVisibility(View.GONE);
                                    findViewById(R.id.showResultsBtn).setEnabled(true);
                                    return false;
                                }
                            })
                            .into(imageView);
                    dialog.dismiss();
                } catch (IOException e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "An error occurred.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Choose a calibration parameter", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (chosenParameter == null)
                findViewById(R.id.showResultsBtn).setEnabled(false);
            dialog.dismiss();
        });
        final AlertDialog alertDialog = builder.create();
        createNewButton.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(CommenceIWAnalysisActivity.this, NewCalibrationParameterActivity.class);
            intent.putExtra("type", "IW");
            startActivity(intent);
        });
        alertDialog.show();
    }

    protected void analyse() {
        progressBar.setVisibility(View.VISIBLE);
        Bitmap grayImage = Analysis.getGrayBitmap(croppedImage);
        float[] convolution = Analysis.getGrayBitmapConvolution(grayImage, chosenParameter.getDelX(), chosenParameter.getDelY());
        float[] calibrationArray = chosenParameter.getCalibrationArray();
        Pair<Integer, Float> p1 = Analysis.getMaxInRange(calibrationArray, 100, 350);
        Pair<Integer, Float> p2 = Analysis.getMaxInRange(calibrationArray, 1000, 1200);
        Pair<Integer, Float> p3 = Analysis.getMaxInRange(calibrationArray, 1500, 1700);
        int pixel1 = p1.first;
        int pixel2 = p2.first;
        int pixel3 = p3.first;
        float[] xNorm = Analysis.getCalibratedArray(pixel1, pixel2, pixel3, 435.12043337f, 545.4795633f, 611.01598333f, calibrationArray.length);
        Intent intent = new Intent(this, IWAnalysisResultsActivity.class);
        intent.putExtra("xNorm", xNorm);
        intent.putExtra("sampleConvolution", convolution);
        progressBar.setVisibility(View.GONE);
        startActivity(intent);
    }

}