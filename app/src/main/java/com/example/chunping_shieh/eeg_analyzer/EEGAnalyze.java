package com.example.chunping_shieh.eeg_analyzer;


import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Handler;

import com.example.chunping_shieh.eeg_analyzer.Constants.InitialConstants;
import com.example.chunping_shieh.eeg_analyzer.DataAnalyzing.EigenV;
import com.example.chunping_shieh.eeg_analyzer.DataAnalyzing.FFT;
import com.example.chunping_shieh.eeg_analyzer.DataStructure.Matrix;
import com.example.chunping_shieh.eeg_analyzer.Graphing.FFT_Graph;
import com.example.chunping_shieh.eeg_analyzer.Graphing.RawEEG_Graph;

import java.io.IOException;

/**
 * This Class Contains the Variables and the Runables
 * */

public class EEGAnalyze extends ActionBarActivity {
//region Variables

    // region Handler and Thread stuff
    protected static HandlerThread mReceiveThread;
    protected static HandlerThread mCalculateThread;
    protected static HandlerThread mAnimateThread;
    protected static HandlerThread plot_FFT;
    private static Handler mUIHandler = new Handler();
    //endregion

    //region Show stuff
    public static TextView showPowerRatio;
    protected static TextView showProgress;
    protected static TextView status;
    protected static TextView showEigenVector;
    protected static TextView showFirstPC;
    protected static TextView showPCA_process;
    //endregion

    //region FFT stuff
    private static int dataCount = 0;
    private static Matrix newData;
    private static double[][] ans;
    //endregion

    //region Ratio stuff
    private static Matrix ratio = new Matrix(InitialConstants.Channel, InitialConstants.RatioDataNumber);
    //endregion

    //region PCA stuff
    private static double firstPC;
    private static EigenV eigenVector;
    private static boolean gotEV = false;
    //endregion

    //region Control flags
    private static long totalTime;
    private static long calculateTime;
    private static int DataLost = 0;
    protected static boolean StopPressed = false;
    private static int UncalculatedData = 0;
    //endregion

    //region BT read stuff
    private static int availableInput;
    private static double[] rcvData = new double[InitialConstants.Channel];
    //endregion

    //region Graph stuff
    protected static RawEEG_Graph rawEEG_graph = new RawEEG_Graph(InitialConstants.Channel, InitialConstants.SampleRate);
    protected static FFT_Graph fft_graph = new FFT_Graph(InitialConstants.Channel, InitialConstants.SampleRate);
    private static int plotTimes = 0;
    //endregion

    //region Button
    protected static Button button;
    public static View.OnClickListener EnableButton;
    protected static View.OnClickListener StartButton;
    protected static View.OnClickListener StopButton;
    //end region

    //region Timer
    static Chronometer timer;
    //endregion

    //region Spinner
    protected static Spinner sampleRateSelect;
    protected static Spinner freshHzSelect;
    //endregion

    //region Progress Bar
    protected static ProgressBar availableBT_bar;
    protected static ProgressBar progressTime_bar;
    protected static ProgressBar uncalculated_bar;
    protected static ProgressBar PCA_process_bar;
    //endregion

    //region Layout
    protected static LinearLayout spinnerFram;
    //endregion


//endregion

//endregion

// Runnables

    /**Shows The loading ... Animation*/
    protected static class loadingAnimate implements Runnable {
        long FrameTime; //time between each dot appears in ms
        int Dots; //No. of dots in the animation
        final CharSequence CurrnetStatus; //Text before dots

        public loadingAnimate(final CharSequence currnetStatus, long frameTime, int dots) {
            CurrnetStatus = currnetStatus;
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    status.setText(currnetStatus);
                }
            });
            FrameTime = frameTime;
            Dots = dots;
        }

        @Override
        public void run() {

            while (!Thread.currentThread().isInterrupted()) {
                for (int i = 0; i < Dots; i++) {
                    try {
                        Thread.sleep(FrameTime);
                        mUIHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                status.setText(status.getText() + ".");
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(CurrnetStatus);
                    }
                });
            }
        }
    }

    /**Enables Bluetooth */
    protected static class enable implements Runnable {
        @Override
        public void run() {
            final String connectStatus = BluetoothConnect.startBT();
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAnimateThread.interrupt();
                    if (connectStatus.equals("Start!")) {
                        status.setText("BT Enabled!");
                        button.setText("Start");
                        button.setOnClickListener(MainActivity.StartButton);
                    } else {
                        status.setText(connectStatus);
                        button.setText("Retry");
                    }
                    button.setClickable(true);
                }

            });
        }
    }

    /**connect Bluetooth */
    protected static class connect implements Runnable {
        @Override
        public void run() {
            if (BluetoothConnect.connectBT()) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        rawEEG_graph.clearChart();
                        button.setText("Stop");
                        button.setOnClickListener(MainActivity.StopButton);
                    }
                });
                Handler mThreadHandler = new Handler(mReceiveThread.getLooper());
                mThreadHandler.post(new GetEEG());
            } else {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        status.setText("Fail to connect Device");
                        spinnerFram.setAlpha(1);
                        sampleRateSelect.setEnabled(true);
                        freshHzSelect.setEnabled(true);
                    }
                });
            }
            mAnimateThread.interrupt();
            button.setClickable(true);
        }
    }

    /**Receive the Bluetooth Input */
    private static class GetEEG implements Runnable {
        boolean NotchState;

        @Override
        public void run() {
            int j;
            StopPressed = false;

            newData = new Matrix(InitialConstants.Channel, (int) (InitialConstants.SampleRate / InitialConstants.FreshHz));
            //The Matrix that holds the received data for the window
            ans = new double[InitialConstants.Channel][(int) (InitialConstants.SampleRate / InitialConstants.FreshHz / 2)];
            // The Matrix that holds the result from FFT

            Handler mAnimateHandler = new Handler(mAnimateThread.getLooper());
            mAnimateHandler.post(new loadingAnimate("Running.", 500, 3));
            Handler mThreadHandler = new Handler(mCalculateThread.getLooper());

            MaintainNotchFilter();

            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    timer.start();
                }
            });

            try {
                while (true) {
                    long totalStart = System.currentTimeMillis();
                    int ratioCount;
                    for (ratioCount = 0; ratioCount < InitialConstants.RatioDataNumber; ratioCount++) {
                        for (j = 0; j < InitialConstants.SampleRate / InitialConstants.FreshHz; j++) {
                            ReadAFram();
                            PlotRawData();
                            mUIHandler.post(new ShowProgress_info());
                        }


                        //Analyze the Data.
                        if (dataCount >= InitialConstants.SampleRate / InitialConstants.FreshHz) {
                            dataCount = 0;
                            mThreadHandler.post(new AnalyzeEEG(ratioCount));
                            UncalculatedData++;
                        }
                    }

                    totalTime = System.currentTimeMillis() - totalStart;
                    //timeShift = timeShift+totalTime-1000;

                }
            } catch (IOException e) {
                e.printStackTrace();
                stop();
            }

        }

        private void ReadAFram() throws IOException {
            if (NotchState != InitialConstants.NotchFilterOn) MaintainNotchFilter();
            CheckAvailableData();
            ReceiveHeader();
            ReceiveChannelMsg();
            BRICommandSet.read_LeadOnOff();
            dataCount++;
        }

        private void PlotRawData() {
            if (++plotTimes >= InitialConstants.SampleRate / InitialConstants.plotTimeRes) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        rawEEG_graph.addNewPoints(rcvData);
                        rawEEG_graph.chartRepaint();
                    }
                });
                plotTimes = 0;
            }
        }

        private void MaintainNotchFilter() {
            NotchState = InitialConstants.NotchFilterOn;
            BluetoothConnect.ControlNotchFilter();
        }

        private void stop() {
            DataLost = 0;
            UncalculatedData = 0;
            if (!StopPressed) {
                mUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        status.setText("BT Disconnected");
                        button.setText("Retry");
                        button.setOnClickListener(MainActivity.StartButton);
                        BRICommandSet.releaseAllResources();
                        timer.stop();
                    }
                });
            }
            mAnimateThread.interrupt();
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    sampleRateSelect.setEnabled(true);
                    freshHzSelect.setEnabled(true);
                    spinnerFram.setAlpha(1);
                }
            });

        }

        private void CheckAvailableData() throws IOException {
            availableInput = BluetoothConnect.btInStream.available();
        }

        private void ReceiveHeader() throws IOException {
            //This function Read and Check the receive header for data lost.
            // If data lost happened, it skip the following content until another header appears, and report the number of data lost.
            int[] header = BRICommandSet.read_header();
            if (header[0] < 240 || header[0] > 255 || InitialConstants.Channel != InitialConstants.ChannelSet[header[1] % 16]) {//if the receive data is not a header...
                while (header[0] < 240 || header[0] > 255 || InitialConstants.Channel != InitialConstants.ChannelSet[header[1] % 16]) {
                    header[0] = header[1];
                    header[1] = BluetoothConnect.btInStream.read();
                    DataLost++;
                    dataCount++;
                }
            }
        }

        private void ReceiveChannelMsg() throws IOException {
            //This function read the channel Info and calculate the voltage for each channel
            int i;
            for (i = 0; i < InitialConstants.Channel; i++) {
                double[] channel_info = BRICommandSet.read_Channel_Data();
                if (channel_info[0] >= 128) {
                    channel_info[0] = channel_info[0] - 256;//signed
                }
                if (InitialConstants.Resolution == 24) {
                    rcvData[i] = 2.4 * (channel_info[0] * 65536 + channel_info[1] * 256 + channel_info[2]) / 8388607;
                } else {
                    rcvData[i] = 2.4 * (channel_info[0] * 65536 + channel_info[1] * 256) / 8388607;
                }


                if (dataCount < newData.getCol()) {
                    newData.setMatrixCell(rcvData[i], i, dataCount);
                }
            }
        }


    }

    /**Analyze EEG Data with FFT, Power Ratio, PCA */
    private static class AnalyzeEEG implements Runnable {

        private int ratioCnt;

        public AnalyzeEEG(int RatioCnt) {
            ratioCnt = RatioCnt;
        }

        @Override
        public void run() {
            long calclulateStart = System.currentTimeMillis();
            if (mReceiveThread.getState() != Thread.State.TERMINATED) {
                CalculateFFT();
                CalculateRatio();
                CalculateEigenV();
                CalculateFirstPC();
            }

            if (mReceiveThread.getState() != Thread.State.TERMINATED) {
                mUIHandler.post(new ShowEEG(ratioCnt));
                UncalculatedData--;
                calculateTime = System.currentTimeMillis() - calclulateStart;
            }
        }

        private void CalculateFFT() {
            //This function calculate the FFT using FFT class.
            //Result is stored in ans[][].
            int j;

            for (j = 0; j < InitialConstants.Channel; j++) {
                ans[j] = FFT.calculateFFT(newData.getRawDouble(j));
            }

            new Handler(plot_FFT.getLooper()).post(new Runnable() {
                @Override
                public void run() {
                    int j;
                    fft_graph.clearChart();
                    for (j = 0; j < InitialConstants.Channel; j++) {
                        fft_graph.addFFTdata(ans[j], j);
                    }
                    fft_graph.chartRepaint();
                }
            });


        }

        private void CalculateRatio() {
            //This function calculate the Power ratio fir each channel.
            //Result is stored in Matrix ratio
            int i, j;
            for (j = 0; j < InitialConstants.Channel; j++) {
                double totle = 0;
                double theta = 0;
                for (i = 0; i < InitialConstants.SampleRate / InitialConstants.FreshHz / 2; i++) {
                    totle += ans[j][i];
                }
                for (i = InitialConstants.getRangeNum(0); i < InitialConstants.getRangeNum(1); i++) {
                    theta += ans[j][i];
                }
                ratio.setMatrixCell(theta / totle, j, ratioCnt);
            }
        }

        private void CalculateEigenV() {
            if (ratioCnt >= 99) {
                eigenVector = new EigenV(Matrix.getScatterMatrix(ratio));
                gotEV = true;
            }
        }

        private void CalculateFirstPC() {
            int j;
            if (gotEV) {
                firstPC = 0;
                for (j = 0; j < InitialConstants.Channel; j++) {
                    firstPC += eigenVector.getMatrixCell(j, 0) * ratio.getMatrixCell(j, ratioCnt);
                }
            }
        }

    }

    /**Plot & Print the raw Data and the Analyzing result */
    private static class ShowEEG implements Runnable {
        private int ratioCnt;

        public ShowEEG(int RatioCt) {
            ratioCnt = RatioCt;
        }

        @Override
        public void run() {
            int j;
            showPowerRatio.setText("F=\n");
            for (j = 0; j < 8; j++) {
                showPowerRatio.setText(showPowerRatio.getText().toString() + ratio.getMatrixCell(j, ratioCnt) + "\n");
            }

            if (gotEV) {
                showEigenVector.setText("Eigen vector: \n");
                for (j = 0; j < eigenVector.getRaw(); j++) {
                    showEigenVector.setText(showEigenVector.getText().toString() + eigenVector.getMatrixCell(j, 0) + "\n");
                }
                showFirstPC.setText("The first PC : \n" + firstPC + "\n");
            }
            showPCA_process.setText("New Eigen vector calculating ... " + ratioCnt + "%");
            PCA_process_bar.setProgress(ratioCnt);

        }
    }

    /**Show the Progress of receiving and Calculating the data */
    private static class ShowProgress_info implements Runnable {
        @Override
        public void run() {
            showProgress.setText("DataLost = " + DataLost + "\n"
                            + "totalTime: " + String.format("%04d", totalTime) + " milliseconds\n"
                            + "calculateTime: " + String.format("%04d", calculateTime) + " milliseconds"
            );


            availableBT_bar.setProgress((int) (0.1 * availableInput));
            progressTime_bar.setProgress((int) (calculateTime / 20 * InitialConstants.FreshHz));
            if (progressTime_bar.getProgress() < 50) {
                progressTime_bar.getProgressDrawable().setColorFilter(null);
            } else {
                progressTime_bar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            }
            uncalculated_bar.setProgress(UncalculatedData);

        }
    }



}
