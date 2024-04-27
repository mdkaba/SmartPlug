package com.example.team7_390_w2024;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import android.widget.Toast;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LogActivity extends AppCompatActivity {
    protected DatabaseHelper dbHelper;
    protected String logType;
    protected Device device;
    protected GridView dataListView;
    protected LineChart lineChart;
    protected LineDataSet dataSet ;
    private TextView dataLogTextView,timestampTextView;

    private Boolean isGraph = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        dbHelper = new DatabaseHelper(getApplicationContext());
        Intent intent = getIntent();
        int id = intent.getIntExtra("DeviceId", -1);
        device = dbHelper.getDevice(id);
    }

    protected void onStart() {
        super.onStart();
        setContentView(R.layout.activity_log);
        Intent intent = getIntent();
        logType = intent.getStringExtra("LogType");
        dataLogTextView=findViewById(R.id.dataLogTextView);
        timestampTextView=findViewById(R.id.timestampTextView);
        //Graph
        lineChart = findViewById(R.id.line_chart);
        lineChart.setVisibility(View.INVISIBLE);

        setupUI();
    }

    private void setupUI() {
        Toolbar logToolbar = findViewById(R.id.logToolbar);
        setSupportActionBar(logToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(logType + " " + getResources().getString(R.string.data_log));
        actionBar.setDisplayHomeAsUpEnabled(true);
        dataListView = findViewById(R.id.logGridView);
        ArrayList<String> arrayList = new ArrayList<>();
        GridItemAdapter arrayAdapter = new GridItemAdapter(this, arrayList);
      
        //Graph Implementation
        List<Entry> chartEntries = new ArrayList<>();
        Date parsedDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd yyyy HH:mm:ss", Locale.ENGLISH);



        if(Objects.equals(logType, "Current")) {
            dataSet = new LineDataSet(chartEntries, "Current Value");
            // Read the database and display the current stored data
            for (CurrentTimestamp item : dbHelper.getAllTimestamps(device)) {
                //Setting values for graph
                try {
                    parsedDate = dateFormat.parse(item.getTimestamp());
                } catch (ParseException e) {
                    // Handle exception if the string cannot be parsed
                    e.printStackTrace();
                    parsedDate = new Date(); // Fallback to current time or handle accordingly
                }

                long timeInMillisSinceEpoch = parsedDate.getTime();
                // Convert timestamp to a relative time (e.g., minutes from reference)
                float floatValue = (float) item.getCurrent();
                chartEntries.add(new Entry(timeInMillisSinceEpoch, floatValue));

                arrayList.add(item.getTimestamp());
                arrayList.add(item.getCurrent() +" mA");
            }
            dataListView.setAdapter(arrayAdapter);

        }
        else
        {
            dataSet = new LineDataSet(chartEntries, "Power Value");
            // Read the database and display the power stored data
          
            for (CurrentTimestamp item : dbHelper.getAllTimestamps(device)) {
                double powerValue = item.getCurrent()*120;

                //Setting values for graph
                try {
                    parsedDate = dateFormat.parse(item.getTimestamp());
                } catch (ParseException e) {
                    // Handle exception if the string cannot be parsed
                    e.printStackTrace();
                    parsedDate = new Date(); // Fallback to current time or handle accordingly
                }

                long timeInMillisSinceEpoch = parsedDate.getTime();
                // Convert timestamp to a relative time (e.g., minutes from reference)
                float floatValue = (float) powerValue;
                chartEntries.add(new Entry(timeInMillisSinceEpoch, floatValue));

                arrayList.add(item.getTimestamp());
                arrayList.add(powerValue+" mW");
            }
            dataListView.setAdapter(arrayAdapter);
        }
        //Show graph
        showGraph();
    }


    private void showGraph()
    {
        XAxis xAxis = lineChart.getXAxis();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setDrawLabels(false); // Remove the labels on the Y-axis on the right
        rightAxis.setDrawAxisLine(false);
        // Disable the grid lines for the right Y-axis if it is used in the chart
        rightAxis.setDrawGridLines(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Create a formatter for the X-axis.
        ValueFormatter dateFormatter = new ValueFormatter() {
            private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {
                // Assuming that the value is a Unix timestamp in milliseconds.
                long millis = (long) value;
                return mFormat.format(new Date(millis));
            }
        };
        xAxis.setValueFormatter(dateFormatter);

        dataSet.setColor(Color.BLUE); // Customize your dataset appearance
        dataSet.setValueTextColor(Color.RED);
        // Create a data object with the data set
        LineData lineData = new LineData(dataSet);
        // Set data to the chart and refresh it
        lineChart.setData(lineData);
        lineChart.invalidate(); // refreshes the chart
        // Customize the chart's appearance (optional)
        lineChart.getDescription().setEnabled(false);
        lineChart.getXAxis().setLabelRotationAngle(-45);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.graph_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.view) {
            //Toggle between list and graph
            if(isGraph)
            {
                dataLogTextView.setVisibility(View.VISIBLE);
                timestampTextView.setVisibility(View.VISIBLE);
                dataListView.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.INVISIBLE);
                isGraph=false;

            }
            else
            {
                lineChart.setVisibility(View.VISIBLE);
                dataLogTextView.setVisibility(View.INVISIBLE);
                timestampTextView.setVisibility(View.INVISIBLE);
                dataListView.setVisibility(View.INVISIBLE);
                isGraph=true;
            }


        }
        return super.onOptionsItemSelected(item);
    }
}


