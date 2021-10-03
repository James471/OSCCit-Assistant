package com.james.igemcancerdetectionapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton startFAB = findViewById(R.id.FABStart);
        startFAB.setOnClickListener(v -> init());
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.actionClearCalibParams) {
            CalibrationParameter.removeAllCalibrationParameter(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void init() {
        Intent intent = new Intent(this, CommenceAnalysisActivity.class);
        startActivity(intent);
    }

}