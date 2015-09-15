package com.example.chunping_shieh.drowsiness_analyzer;


import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.chunping_shieh.drowsiness_analyzer.Bluetooth.BRICommandSet;
import com.example.chunping_shieh.drowsiness_analyzer.Bluetooth.BluetoothConnect;
import com.example.chunping_shieh.drowsiness_analyzer.Constants.InitialConstants;
import com.example.chunping_shieh.drowsiness_analyzer.DataAnalyzing.EigenV;
import com.example.chunping_shieh.drowsiness_analyzer.DataAnalyzing.FFT;
import com.example.chunping_shieh.drowsiness_analyzer.DataStructure.Matrix;
import com.example.chunping_shieh.drowsiness_analyzer.Graphing.FFT_Graph;
import com.example.chunping_shieh.drowsiness_analyzer.Graphing.RawEEG_Graph;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This Class Contains the Variables and the Runables
 * */

public class EEGAnalyze extends ActionBarActivity {
//region Variables

    // region Handler and Thread stuff
    protected static HandlerThread mReceiveThread;
    protected static HandlerThread mCalculateThread;
    protected static HandlerThread mEigenvectorThread;
    protected static HandlerThread mAnimateThread;
    protected static HandlerThread plot_EEG;
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
    private static StringBuilder EV;
    private static StringBuilder PR;
    private static StringBuilder PC;
    //endregion

    //region FFT stuff
    private static int dataCount = 0;
    private static Matrix newData[] = new Matrix[2];
    private static Matrix newDataAll;
    private static double[][] ans;
    //endregion

    //region Ratio stuff
    private static Matrix ratio = new Matrix(InitialConstants.Channel, InitialConstants.RatioDataNumber);
    //endregion

    //region PCA stuff
    //private static double firstPC;
    private static Matrix PCs;
    private static EigenV eigenVector = new EigenV();
    //end region

    //region moving average stuff
    private static double[] firstPC_Array = new double[InitialConstants.MovingAverageWindowWidth];
    private static int MovingAverageCnt = 0;
    private static double finalResult;
    //endregion

    //region Control flags
    private static int DataLost = 0;
    protected static boolean StopPressed = false;
    private static int UncalculatedData = 0;
    //endregion

    //region BT read stuff
    private static int availableInput;
    private static double[] rcvData = new double[InitialConstants.Channel];
    //endregion

    //region Graph stuff
    protected static RawEEG_Graph rawEEG_graph = new RawEEG_Graph();
    protected static FFT_Graph fft_graph = new FFT_Graph();
    private static int addTimes = 0;
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
    protected static ProgressBar uncalculated_bar;
    protected static ProgressBar PCA_process_bar;
    //endregion

    //region RunTime
    protected static RunTimeTest totalTime = new RunTimeTest();
    protected static RunTimeTest calculateTime = new RunTimeTest();
    protected static RunTimeTest FFT_runTime = new RunTimeTest();
    protected static RunTimeTest PowerRatio_runTime = new RunTimeTest();
    protected static RunTimeTest Eigenvector_runTime = new RunTimeTest();
    protected static RunTimeTest PrincipleComponents_runTime = new RunTimeTest();
    protected static RunTimeTest MovingAverage_runtime = new RunTimeTest();

    //endregion

    //region Layout
    protected static LinearLayout spinnerFram;
    //endregion


    //region Files
    protected static FileOutputStream fileOutputStream;
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
        int newDataCount = 0;

        @Override
        public void run() {
            int j;
            StopPressed = false;
            newData[0] = new Matrix(InitialConstants.Channel, (int) (InitialConstants.SampleRate / InitialConstants.FreshHz /2));
            newData[1] = new Matrix(InitialConstants.Channel, (int) (InitialConstants.SampleRate / InitialConstants.FreshHz /2));
            newDataAll = new Matrix(InitialConstants.Channel, (int) (InitialConstants.SampleRate / InitialConstants.FreshHz));
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
                    for (int ratioCount = 0; ratioCount < InitialConstants.RatioDataNumber; ratioCount++) {
                        totalTime.tic();
                        for (j = 0; j < InitialConstants.SampleRate / InitialConstants.FreshHz / 2; j++) {
                            ReadAFram();
                            PlotRawData();
                            mUIHandler.post(new ShowProgress_info());
                        }

                        //Analyze the Data.
                        if (dataCount >= InitialConstants.SampleRate / InitialConstants.FreshHz / 2) {
                            dataCount = 0;
                            newDataAll.combineMatrix(newData[newDataCount],newData[(newDataCount+1)%2]);
                            mThreadHandler.post(new AnalyzeEEG(ratioCount,newDataAll));
                            UncalculatedData++;
                        }
                        if(++newDataCount > 1) newDataCount = 0;
                        totalTime.toc();
                    }


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
            if (++addTimes >= InitialConstants.SampleRate / InitialConstants.plotRes) {
                rawEEG_graph.addNewPoints(rcvData);
                addTimes = 0;
            }
            if (++plotTimes >= InitialConstants.SampleRate / InitialConstants.plotTimeRes){
                new Handler(plot_EEG.getLooper()).post(new Runnable() {
                    @Override
                    public void run() {rawEEG_graph.chartRepaint();
                    }
                });
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


                if (dataCount < newData[newDataCount].getCol()) {
                    newData[newDataCount].setMatrixCell(rcvData[i], i, dataCount);
                }
            }
        }


    }

    /**Analyze EEG Data with FFT, Power Ratio, PCA */
    private static class AnalyzeEEG implements Runnable {

        private int ratioCnt;
        private Matrix inputData;

        public AnalyzeEEG(int RatioCnt, Matrix InputData) {
            ratioCnt = RatioCnt;
            inputData = InputData;
        }

        @Override
        public void run() {
            calculateTime.tic();
            if (mReceiveThread.getState() != Thread.State.TERMINATED) {
                CalculateFFT();
                CalculateRatio();
                CalculatePC();
                CalculateMovingAverage();
            }

            if (mReceiveThread.getState() != Thread.State.TERMINATED) {
                mUIHandler.post(new ShowEEG(ratioCnt));
                UncalculatedData--;
            }
            calculateTime.toc();
        }

        private void CalculateFFT() {
            //This function calculate the FFT using FFT class.
            //Result is stored in ans[][].
            FFT_runTime.tic();
            for (int j = 0; j < InitialConstants.Channel; j++) {
                ans[j] = FFT.calculateFFT(inputData.getRawDouble(j));
            }

            fft_graph.clearChart();
            for (int j = 0; j < InitialConstants.Channel; j++) {
                fft_graph.addFFTdata(ans[j], j);
            }
            new Handler(plot_FFT.getLooper()).post(new Runnable() {
                @Override
                public void run() {fft_graph.chartRepaint();
                }
            });
            FFT_runTime.toc();

        }

        private void CalculateRatio() {
            //This function calculate the Power ratio fir each channel.
            //Result is stored in Matrix ratio
            PowerRatio_runTime.tic();
            for (int j = 0; j < InitialConstants.Channel; j++) {
                double beta = 0;
                double theta = 0;
                for (int i = InitialConstants.getBetaLow(); i < InitialConstants.getBetaHigh(); i++) {
                    beta += ans[j][i];
                }
                for (int i = InitialConstants.getThetaLow(); i < InitialConstants.getThetaHigh(); i++) {
                    theta += ans[j][i];
                }
                ratio.setMatrixCell(theta / beta, j, ratioCnt);
            }

            PR = new StringBuilder();
            for (int j = 0; j < ratio.getRaw(); j++) {
                PR.append(String.format("%.2f", ratio.getMatrixCell(j, ratioCnt))).append("\n");
            }
            PowerRatio_runTime.toc();
        }

        private void CalculatePC() {

            PrincipleComponents_runTime.tic();
            PCs = Matrix.MatrixCross(eigenVector.Transpose(),ratio.getColVector(ratioCnt));
            PC = new StringBuilder();
            for (int PC_No = 0; PC_No < PCs.getRaw(); PC_No++){
                if (Double.isNaN(PCs.getMatrixCell(PC_No,0)))break;
                PC.append(String.format("%+.3f\n", PCs.getMatrixCell(PC_No, 0)));
            }
            PrincipleComponents_runTime.toc();

            UncalculatedData++;
            new Handler(mEigenvectorThread.getLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Eigenvector_runTime.tic();
                    eigenVector = new EigenV(ratio.getCovarianceMatrix());

                    EV = new StringBuilder();
                    for (int rawNo = 0; rawNo < eigenVector.getRaw(); rawNo++) {
                        for (int colNo = 0; colNo < eigenVector.getCol(); colNo++) {
                            if (Double.isNaN(eigenVector.getMatrixCell(rawNo,colNo))){
                                break;
                            }
                            EV.append(String.format("%+.2f\t", eigenVector.getMatrixCell(rawNo, colNo)));
                        }
                        EV.append("\n");
                    }
                    mUIHandler.post(new ShowEigenV());

                    Eigenvector_runTime.toc();
                    UncalculatedData--;
                }
            });
        }

        private void CalculateMovingAverage(){
            MovingAverage_runtime.tic();
            firstPC_Array[MovingAverageCnt] = PCs.getMatrixCell(0,0);
            if(++MovingAverageCnt >= InitialConstants.MovingAverageWindowWidth)MovingAverageCnt = 0;
            finalResult = 0;
            for (double aFirstPC_Array : firstPC_Array) {
                finalResult += aFirstPC_Array / firstPC_Array.length;
            }
            MovingAverage_runtime.toc();
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

            showPowerRatio.setText("PR =\n"+PR);

            showFirstPC.setText("PCs : \n" + PC);

            showPCA_process.setText("Final Result :"+String.format("%.3f",finalResult)+"    ratioCnt :  " + ratioCnt);
            PCA_process_bar.setProgress(ratioCnt * 100 / InitialConstants.RatioDataNumber);

//            try {
//                fileOutputStream.write(String.format("%d\t%.3f\n",System.currentTimeMillis(),finalResult).getBytes());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }
    }

    /**Show the Eigenvector */
    private static class ShowEigenV implements Runnable{

        @Override
        public void run() {

            showEigenVector.setText("Eigenvector = \n"+EV);
        }
    }

    /**Show the Progress of receiving and Calculating the data */
    private static class ShowProgress_info implements Runnable {
        @Override
        public void run() {
            showProgress.setText("DataLost = " + DataLost + "\n"
                            + "totalTime:\t\t" + String.format("%06d", totalTime.averageRunTime()) + " \n"
                            + "Eigenvector:\t" + String.format("%06d", Eigenvector_runTime.averageRunTime()) + "\n"
                            + "calculate:\t\t" + String.format("%06d", calculateTime.averageRunTime()) + " \n"
                            + "FFT:\t" + String.format("%06d", FFT_runTime.averageRunTime()) + "\t\t"
                            + "PR:\t" + String.format("%06d", PowerRatio_runTime.averageRunTime()) + "\n"
                            + "PC:\t" + String.format("%06d", PrincipleComponents_runTime.averageRunTime())+"\t\t"
                            + "MA:\t" + String.format("%06d", MovingAverage_runtime.averageRunTime())

            );


            availableBT_bar.setProgress((int) (0.1 * availableInput));
//            progressTime_bar.setProgress((int) (calculateTime.averageRunTime() / 20 * InitialConstants.FreshHz));
//            if (progressTime_bar.getProgress() < 50) {
//                progressTime_bar.getProgressDrawable().setColorFilter(null);
//            } else {
//                progressTime_bar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//            }
            uncalculated_bar.setProgress(UncalculatedData);

        }
    }



}
