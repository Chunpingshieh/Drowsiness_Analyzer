package com.example.chunping_shieh.drowsiness_analyzer;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.example.chunping_shieh.drowsiness_analyzer.Bluetooth.BRICommandSet;
import com.example.chunping_shieh.drowsiness_analyzer.Constants.InitialConstants;

import org.achartengine.ChartFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ChunPing-Shieh on 2015/7/28.
 * This Class Contains Services Listeners to the Buttons and Switches on the Screen
 */
public class MainActivity extends EEGAnalyze {

    //region Services
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        SetUpText();
        SetUpButton();
        SetUpTimer();
        SetUpSwitch();
        SetUpSpinner();
        SetUpProgressbarar();
        SetUpThreads();
        //SetUpFileStorage();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(rawEEG_graph.getmChartView() == null){
            LinearLayout layout = (LinearLayout) findViewById(R.id.rawEEG_chart);
            rawEEG_graph.setmChartView(ChartFactory.getLineChartView(this, rawEEG_graph.getmDataset(), rawEEG_graph.getmRenderer()));
            layout.addView(rawEEG_graph.getmChartView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }else {
            rawEEG_graph.chartRepaint();
        }

        if (fft_graph.getmChartView() ==  null){
            LinearLayout layout = (LinearLayout) findViewById(R.id.FFT_chart);
            fft_graph.setmChartView(ChartFactory.getLineChartView(this, fft_graph.getmDataset(), fft_graph.getmRenderer()));
            layout.addView(fft_graph.getmChartView(), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }else {
            fft_graph.chartRepaint();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop(){
        BRICommandSet.releaseAllResources();
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mReceiveThread.quit();
        mCalculateThread.quit();
        mAnimateThread.quit();
    }

    @Override
    public void onBackPressed(){

    }


    //SetUp=====================================================================

    private void SetUpText(){
        showPowerRatio = (TextView) findViewById(R.id.PowerRatio);
        showProgress = (TextView) findViewById(R.id.ProgressTime);
        status = (TextView)findViewById(R.id.status);
        showEigenVector = (TextView)findViewById(R.id.EigenVector);
        showFirstPC = (TextView)findViewById(R.id.FirstPC);
        showProgress.setText("DataLost = \ntotalTime: \ncalculateTime:");
        showPCA_process = (TextView)findViewById(R.id.PCA_Process);
    }
    private void SetUpButton(){
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(EnableButton);
    }
    private void SetUpTimer(){
        timer = (Chronometer)findViewById(R.id.timer);
    }
    private void SetUpSwitch(){
        Switch notchFilterOn = (Switch) findViewById(R.id.notchFilter);
        notchFilterOn.setOnCheckedChangeListener(swhNotchFilter);
    }
    private void SetUpSpinner(){
        sampleRateSelect = (Spinner)findViewById(R.id.SampleRate);
        sampleRateSelect.setAdapter(new ArrayAdapter<Integer>(this, R.layout.myspinner, InitialConstants.channelSet));
        sampleRateSelect.setOnItemSelectedListener(spnSampleRate);

        freshHzSelect = (Spinner)findViewById(R.id.FreshHz);
        freshHzSelect.setAdapter(new ArrayAdapter<Double>(this, R.layout.myspinner, InitialConstants.freshHzSet));
        freshHzSelect.setOnItemSelectedListener(spnFreshHz);

        spinnerFram = (LinearLayout)findViewById(R.id.SpinnersFrame);
    }
    private void SetUpProgressbarar(){
        availableBT_bar = (ProgressBar)findViewById(R.id.AvailableBT);
        //progressTime_bar = (ProgressBar)findViewById(R.id.CalculateTime);
        uncalculated_bar = (ProgressBar)findViewById(R.id.UncalculateData);
        PCA_process_bar = (ProgressBar)findViewById(R.id.PCA_progressBar);
    }
    private void SetUpThreads(){
        mCalculateThread = new HandlerThread("calculate");
        mCalculateThread.start();
        mAnimateThread = new HandlerThread("animate");
        mAnimateThread.start();
        mReceiveThread = new HandlerThread("receive");
        mReceiveThread.setPriority(Thread.MAX_PRIORITY);
        mReceiveThread.start();
        mEigenvectorThread = new HandlerThread("eigenvector");
        mEigenvectorThread.start();
        plot_FFT = new HandlerThread("plotFFT");
        plot_FFT.start();
        plot_EEG = new HandlerThread("plotEEG");
        plot_EEG.setPriority(Thread.MAX_PRIORITY);
        plot_EEG.start();

    }
    private void SetUpFileStorage(){
        try {
            fileOutputStream = openFileOutput("Drowsiness_Level.txt", Context.MODE_PRIVATE);
            String test = "test";
            fileOutputStream.write(test.getBytes());
            fileOutputStream.close();
            
            
            FileInputStream fileInputStream = openFileInput("Drowsiness_Level.txt");
            byte[] bufByte = new byte[10];

            while (true){
                int c = fileInputStream.read(bufByte);
                if (c == -1)break;
                else showPowerRatio.append(new String(bufByte),0,c);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //endregion


    //Listener=========================================================

    static/**Button Listener*/ {

        EnableButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setClickable(false);
                Handler mAnimateHandler = new Handler(mAnimateThread.getLooper());
                mAnimateHandler.post(new loadingAnimate("Enabling", 100, 10));
                Handler mThreadHandler = new Handler(mReceiveThread.getLooper());
                mThreadHandler.post(new enable());
            }
        };

        StartButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setClickable(false);
                mReceiveThread = new HandlerThread("receive");
                mReceiveThread.start();
                Handler mAnimateHandler = new Handler(mAnimateThread.getLooper());
                mAnimateHandler.post(new loadingAnimate("Connecting", 100, 10));
                spinnerFram.setAlpha((float) 0.2);
                Handler mThreadHandler = new Handler(mReceiveThread.getLooper());
                sampleRateSelect.setEnabled(false);
                freshHzSelect.setEnabled(false);
                mThreadHandler.post(new connect());
            }
        };

        StopButton = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setClickable(false);
                StopPressed = true;
                BRICommandSet.releaseAllResources();
                timer.stop();
                timer.setBase(SystemClock.elapsedRealtime());
                button.setText("Start");
                button.setOnClickListener(StartButton);
                mReceiveThread.quit();
                button.setClickable(true);
                status.setText("Stopped");
            }
        };

    }

    private static Spinner.OnItemSelectedListener spnSampleRate = new Spinner.OnItemSelectedListener(){

        public void onItemSelected(AdapterView parent, View view, int position, long id) {
            InitialConstants.SampleRate = (int) parent.getSelectedItem();
        }

        public void onNothingSelected(AdapterView parent) {
        }
    };

    private static Spinner.OnItemSelectedListener spnFreshHz = new Spinner.OnItemSelectedListener(){
        public void onItemSelected(AdapterView parent, View view, int position, long id){
            InitialConstants.FreshHz = (double) parent.getSelectedItem();
        }

        public void onNothingSelected(AdapterView parent){
        }
    };

    private Switch.OnCheckedChangeListener swhNotchFilter = new Switch.OnCheckedChangeListener(){

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            InitialConstants.NotchFilterOn = isChecked;
        }
    };





}
