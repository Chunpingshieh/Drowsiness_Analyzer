package com.example.chunping_shieh.eeg_analyzer.Graphing;

import android.graphics.Color;

import com.example.chunping_shieh.eeg_analyzer.Constants.InitialConstants;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by ChunPing-Shieh on 2015/7/20.
 */
public class FFT_Graph extends lineGraph {
    public FFT_Graph(int Channel, int SampleRate) {
        super(Channel, SampleRate);
        width = 100;
        mRenderer.setAntialiasing(false);
        // Add single dataset to multiple dataset

        int[] color = {Color.RED, Color.RED, Color.YELLOW, Color.BLUE, Color.CYAN, Color.YELLOW, Color.GREEN, Color.GREEN};
        String[] title = {"Fp1", "Fp2","Fz", "C3", "C4", "Pz", "O1", "O2"};
        int i;
        for (i = 0; i < channel; i++) {
            XYSeries dataset = new XYSeries(title[i]);
            mDataset.addSeries(dataset);
            XYSeriesRenderer renderer = new XYSeriesRenderer();
            renderer.setColor(color[i]);
            renderer.setPointStyle(PointStyle.POINT);
            renderer.setFillPoints(true);
            mRenderer.addSeriesRenderer(renderer);
        }
        mRenderer.setBackgroundColor(Color.BLACK);
        mRenderer.setApplyBackgroundColor(true);
        mRenderer.setZoomEnabled(true);
        mRenderer.setPanEnabled(true, true);
        mRenderer.setYAxisMax(100);
        mRenderer.setYAxisMin(0);
        mRenderer.setXAxisMax(width);
        mRenderer.setPanEnabled(false);
        mRenderer.setClickEnabled(false);
        mRenderer.setZoomEnabled(false,false);
    }


    public void addFFTdata(double[] FFTdata, int channelNum){
        int i;
        for (i = 0; i < FFTdata.length; i++){
            mDataset.getSeriesAt(channelNum).add(i* InitialConstants.FreshHz,FFTdata[i]);
        }
    }
}
