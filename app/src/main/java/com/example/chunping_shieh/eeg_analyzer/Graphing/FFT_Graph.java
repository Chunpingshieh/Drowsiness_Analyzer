package com.example.chunping_shieh.eeg_analyzer.Graphing;

import android.graphics.Color;

import com.example.chunping_shieh.eeg_analyzer.Constants.InitialConstants;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by ChunPing-Shieh on 2015/7/20.
 * FFT_Graph defines the details of the line graph that shows FFT result of the raw Data
 * This Class is powered by aChartEngine
 */
public class FFT_Graph extends lineGraph {
    public FFT_Graph() {
        super();
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
        mRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));
        mRenderer.setShowLegend(false);
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
