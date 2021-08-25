package com.james.igemcancerdetectionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_results);

        Intent intent = getIntent();
        float[] xNorm = intent.getFloatArrayExtra("xNorm");
        float[] sampleConvolution = intent.getFloatArrayExtra("sampleConvolution");

        LineChart resultChart = findViewById(R.id.resultPlot);
        List<Entry> entries = new ArrayList<>();

        for (int i = 0; i < xNorm.length; i++) {
            entries.add(new Entry(xNorm[i], sampleConvolution[i]));
        }

        LineDataSet lineDataSet =new LineDataSet(entries, "Result");
        resultChart.setData(new LineData(lineDataSet));

    }
}