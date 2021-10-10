package com.james.osccitassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import osccitassistant.R;

public class CommenceICAnalysisActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ICCalibrationParameter[] parameters;
    ICCalibrationParameter chosenParameter;
    Bitmap croppedImage;
    List<Float> knownIntensities = new ArrayList<>();
    List<Float> knownConcentrations = new ArrayList<>();
    float unknownSampleIntensity;


    ActivityResultLauncher<Intent> knownConcImagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
        if(activityResult.getResultCode() == Activity.RESULT_OK) {
            Intent resultData = activityResult.getData();
            if (resultData != null) {
                Uri fullImageUri = resultData.getData();
                showImageConcDialog(fullImageUri, true);
            }
        }
    });

    ActivityResultLauncher<Intent> unknownConcImagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), activityResult -> {
        if(activityResult.getResultCode() == Activity.RESULT_OK) {
            Intent resultData = activityResult.getData();
            if (resultData != null) {
                Uri fullImageUri = resultData.getData();
                showImageConcDialog(fullImageUri, false);
            }
        }
    });

    ActivityResultLauncher<String> requestStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if(isGranted) {
            Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
            chooseImage.setType("image/*");
            chooseImage = Intent.createChooser(chooseImage, "Pick an image");
            knownConcImagePickLauncher.launch(chooseImage);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Storage Permission Needs To Be Given", Toast.LENGTH_SHORT);
            toast.show();
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commence_icanalysis);

        findViewById(R.id.btnChooseParam).setOnClickListener(view -> showChooseCalibrationParameterDialog());
        findViewById(R.id.btnAddImage).setOnClickListener(view -> getNewImage(true));
        findViewById(R.id.btnAddUnknownSampleImage).setOnClickListener(view -> getNewImage(false));
        findViewById(R.id.btnGetConc).setOnClickListener(view -> launchAnalysisResultsActivity());

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        chosenParameter = parameters[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    protected void launchAnalysisResultsActivity() {
        int size = knownIntensities.size();
        float[] intensity = new float[size];
        float[] concentration = new float[size];
        for(int i = 0; i<size; i++) {
            intensity[i] = knownIntensities.get(i);
            concentration[i] = knownConcentrations.get(i);
        }
        Intent intent = new Intent(CommenceICAnalysisActivity.this, ICAnalysisResultsActivity.class);
        intent.putExtra("KnownIntensities", intensity);
        intent.putExtra("Concentrations", concentration);
        intent.putExtra("UnknownSampleIntensity", unknownSampleIntensity);
        startActivity(intent);

    }

    protected void getNewImage(boolean isConcKnown) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            Intent chooseImage = new Intent(Intent.ACTION_GET_CONTENT);
            chooseImage.setType("image/*");
            chooseImage = Intent.createChooser(chooseImage, "Pick an image");
            if (isConcKnown)
                knownConcImagePickLauncher.launch(chooseImage);
            else
                unknownConcImagePickLauncher.launch(chooseImage);
        }
    }

    @SuppressLint("SetTextI18n")
    protected void addCurrentEntry(float average, float concentration) {
        if(concentration!=-100) {
            knownIntensities.add(average);
            knownConcentrations.add(concentration);
        }

        LinearLayout tableScrollView = findViewById(R.id.tableScrollView);

        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        entryLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
        textViewParams.setMargins(3, 3, 3, 3);

        TextView averageTextView = new TextView(this);
        averageTextView.setLayoutParams(textViewParams);
        averageTextView.setText(Float.toString(average));

        entryLayout.addView(averageTextView);

        TextView concentrationTextView = new TextView(this);
        if(concentration!=-100)
            concentrationTextView.setText(Float.toString(concentration));
        else
            concentrationTextView.setText("?");
        concentrationTextView.setLayoutParams(textViewParams);

        entryLayout.removeAllViews();
        entryLayout.addView(averageTextView);
        entryLayout.addView(concentrationTextView);

        tableScrollView.addView(entryLayout);

        findViewById(R.id.btnChooseParam).setEnabled(false);

        if(knownConcentrations.size()>2)
            findViewById(R.id.btnAddUnknownSampleImage).setEnabled(true);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    protected void showImageConcDialog(Uri fullImageUri, boolean isConcKnown) {
        final View dialogShowICImageLayout = getLayoutInflater().inflate(R.layout.dialog_show_ic_image, null);
        if(!isConcKnown)
            dialogShowICImageLayout.findViewById(R.id.edtTxtConc).setVisibility(View.GONE);
        AlertDialog.Builder builder = new AlertDialog.Builder(CommenceICAnalysisActivity.this);
        builder.setView(dialogShowICImageLayout);
        builder.setPositiveButton("Continue", (dialog, which) -> {
            if(isConcKnown) {
                EditText editText = dialogShowICImageLayout.findViewById(R.id.edtTxtConc);
                String stringConc = editText.getText().toString();
                if (!stringConc.isEmpty()) {
                    try {
                        float concentration = Float.parseFloat(stringConc);
                        float average = Analysis.getGrayBitmapAverage(croppedImage);
                        addCurrentEntry(average, concentration);
                        Toast.makeText(this, "Entry added", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Concentration must be a double", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Please enter the concentration", Toast.LENGTH_SHORT).show();
                }
            } else {
                unknownSampleIntensity = Analysis.getGrayBitmapAverage(croppedImage);
                Toast.makeText(this, "Image obtained", Toast.LENGTH_LONG).show();
                addCurrentEntry(unknownSampleIntensity, -100);
                findViewById(R.id.btnAddImage).setEnabled(false);
                findViewById(R.id.btnAddUnknownSampleImage).setEnabled(false);
                findViewById(R.id.btnGetConc).setEnabled(true);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Toast.makeText(this, "Cancelled by user", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });
        ExifInterface exifInterface;
        final AlertDialog alertDialog = builder.create();
        try {
            InputStream inputStream = getContentResolver().openInputStream(fullImageUri);
            exifInterface = new ExifInterface(inputStream);
            int rotation = Analysis.exifToDegrees(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL));
            Rect rect = chosenParameter.getCalibrationRectangle();
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap fullImage = getBitmapFromUri(fullImageUri);
            fullImage = Bitmap.createScaledBitmap(fullImage, chosenParameter.getBitmapWidth(), chosenParameter.getBitmapHeight(), true);
            croppedImage = Bitmap.createBitmap(fullImage, rect.left, rect.top, rect.right-rect.left, rect.bottom-rect.top, matrix, true);
            ImageView imageView = dialogShowICImageLayout.findViewById(R.id.icImageView);
            alertDialog.show();
            Glide.with(getApplicationContext())
                    .load(croppedImage)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(getApplicationContext(), "Load failed", Toast.LENGTH_LONG).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(imageView);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void showChooseCalibrationParameterDialog() {
        parameters = ICCalibrationParameter.getAllICCalibrationParameters(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.dilog_get_calibration_parameter, null);
        Spinner spinner = customLayout.findViewById(R.id.spinnerParamList);
        List<String> spinnerArray = new ArrayList<>();
        if(parameters!=null) {
            for (ICCalibrationParameter parameter : parameters) {
                spinnerArray.add(parameter.getName());
            }
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, spinnerArray);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);
        Button buttonCreateNewParam = customLayout.findViewById(R.id.btnCreateParam);
        AlertDialog.Builder builder = new AlertDialog.Builder(CommenceICAnalysisActivity.this);
        builder.setView(customLayout);
        builder.setPositiveButton("Continue", (dialog, which) -> {
            if(chosenParameter!=null) {
                findViewById(R.id.btnAddImage).setEnabled(true);
                dialog.dismiss();
            }
            else {
                Toast.makeText(getApplicationContext(), "Choose a calibration parameter", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if(chosenParameter==null)
                findViewById(R.id.btnAddImage).setEnabled(false);
            dialog.dismiss();
        });
        final AlertDialog alertDialog = builder.create();
        buttonCreateNewParam.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent(CommenceICAnalysisActivity.this, NewCalibrationParameterActivity.class);
            intent.putExtra("type", "IC");
            startActivity(intent);
        });
        alertDialog.show();
    }

}