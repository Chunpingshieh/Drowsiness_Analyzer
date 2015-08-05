package com.example.chunping_shieh.eeg_analyzer.Graphing;

import android.graphics.Color;

import com.example.chunping_shieh.eeg_analyzer.Constants.InitialConstants;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * Created by ChunPing-Shieh on 2015/7/20.
 *  RawEEG_Graph defines the details of the line graph that shows FFT result of the raw Data
 *  This Class is powered by aChartEngine
 */
public class RawEEG_Graph extends lineGraph {
    public RawEEG_Graph() {
        super();
        width = 3;
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
        mRenderer.setZoomEnabled(true);
        mRenderer.setPanEnabled(true, true);
        mRenderer.setYAxisMax(10);
        mRenderer.setYAxisMin(-1);
        mRenderer.setXAxisMax(width);
        mRenderer.setPanEnabled(false);
        mRenderer.setClickEnabled(false);
        mRenderer.setZoomEnabled(false, false);

    }

    public void addNewPoints(double[] data)
    {

        if (time>=width){
            clearChart();
        }

        int i;
        for (i = 0; i < channel; i++){
            mDataset.getSeriesAt(i).add(time,data[i]+channel-i);
        }
        time = time+(double) 1/InitialConstants.plotTimeRes;
    }
}
