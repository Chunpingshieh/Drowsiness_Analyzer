package com.example.chunping_shieh.drowsiness_analyzer;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class DrowsinessTestActivity extends ActionBarActivity  {

    private static TextView cue;
    private static Button pop;
    private static Handler mUIHandler = new Handler();
    private static HandlerThread mThread;
    private static Handler mHandler;
    private static File file;
    private static FileOutputStream outputStream;
    private static RunTimeTest reactionTime = new RunTimeTest(0);




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_drowsiness_test);
        cue = (TextView)findViewById(R.id.Cue);
        pop = (Button)findViewById(R.id.ReactButton);
        mThread = new HandlerThread("showCue");
        mThread.start();
        mHandler = new Handler(mThread.getLooper());
        SetUpFileStorage();
    }
    private void SetUpFileStorage(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),"React.txt");
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    /**Runnable*/
    private static class giveCue implements Runnable{

        @Override
        public void run() {

            reactionTime.tic();
            try {
                Thread.sleep((long) (Math.random()*500+500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                outputStream.write(String.format("%d\t", System.currentTimeMillis()).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

            reactionTime.tic();

            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    cue.setAlpha(1);
                    pop.setClickable(true);
                }
            });
        }
    }




    /**OnClick Listener*/
    public void popIt(View view){
        try {
            outputStream.write(String.format("%d\r\n",reactionTime.tocTime()).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        cue.setAlpha(0);
        pop.setClickable(false);
        mHandler.post(new giveCue());
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_drowsiness_test, menu);
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
    public void onBackPressed(){
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
        SCAN();
        EEGAnalyze.mRecordThread.interrupt();
        super.onBackPressed();
    }


    public void SCAN(){
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

}
