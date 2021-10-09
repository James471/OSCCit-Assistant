package com.james.osccitassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import osccitassistant.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AdapterView.OnItemClickListener itemClickListener = (adapterView, view, i, l) -> {
            if (i == 0) {
                Intent intent = new Intent(MainActivity.this, CommenceIWAnalysisActivity.class);
                startActivity(intent);
            } else if (i == 1) {
                Intent intent = new Intent(MainActivity.this, CommenceICAnalysisActivity.class);
                startActivity(intent);
            }
        };
        ListView listView = findViewById(R.id.list_view);
        listView.setOnItemClickListener(itemClickListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.actionClearIntensityWavelengthCalibParams) {
            IWCalibrationParameter.removeIWAllCalibrationParameter(this);
            Toast.makeText(this, "All Calibration Parameters Removed", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.actionClearIntensityConcCalibParams) {
            ICCalibrationParameter.removeICAllCalibrationParameter(this);
            Toast.makeText(this, "All calibration parameters removed", Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }

}