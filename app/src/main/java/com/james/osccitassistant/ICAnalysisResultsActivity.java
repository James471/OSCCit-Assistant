package com.james.osccitassistant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;
import java.util.List;

import osccitassistant.R;

public class ICAnalysisResultsActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icanalysis_results);

        Intent intent = getIntent();
        float[] knownIntensities = intent.getFloatArrayExtra("KnownIntensities");
        float[] concentrations = intent.getFloatArrayExtra("Concentrations");
        float unknownSampleIntensity = intent.getFloatExtra("UnknownSampleIntensity", 0);

        float[] params = Analysis.getLinearFitParameters(concentrations, knownIntensities);
        float m = params[0];
        float c = params[1];
        float sigM = params[2];
        float sigC = params[3];

        float[] result = Analysis.linearFunctionInverse(m, c, sigM, sigC, unknownSampleIntensity);
        float unknownConcentration = result[0];
        float sigUnknownConcentration = result[1];

        CombinedChart combinedChart = findViewById(R.id.icResultPlot);
        List<Entry> entry1 = new ArrayList<>();
        for(int i = 0; i<knownIntensities.length; i++) {
            entry1.add(new Entry(concentrations[i], knownIntensities[i]));
        }
        List<Entry> entry2 = new ArrayList<>();
        entry2.add(new Entry(concentrations[0], m*concentrations[0]+c));
        entry2.add(new Entry(concentrations[concentrations.length-1], m*concentrations[concentrations.length-1]+c));

        ScatterDataSet scatterDataSet = new ScatterDataSet(entry1, "Data Points");
        int greenColor = ContextCompat.getColor(this, R.color.green);
        scatterDataSet.setColor(greenColor);
        LineDataSet lineDataSet = new LineDataSet(entry2, "Best Fit");
        lineDataSet.setDrawValues(false);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        int blueColor = ContextCompat.getColor(this, R.color.blue);
        lineDataSet.setColor(blueColor);

        ScatterData scatterData = new ScatterData(scatterDataSet);
        LineData lineData = new LineData(lineDataSet);

        CombinedData combinedData = new CombinedData();
        combinedData.setData(scatterData);
        combinedData.setData(lineData);
        combinedChart.setData(combinedData);
        combinedChart.getXAxis().setSpaceMax(0.1f);
        combinedChart.getXAxis().setSpaceMin(0.1f);

        TextView icAnalysisResult = findViewById(R.id.icAnalysisResultTxtVw);
        icAnalysisResult.setText("Concentration="+unknownConcentration);

    }

}