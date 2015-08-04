package com.example.chunping_shieh.eeg_analyzer.Graphing;

/**
 * Created by ChunPing-Shieh on 2015/4/30.
 */


import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;


public class lineGraph {

    private GraphicalView mChartView;

    protected XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    protected double time = 0;

    protected XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer(); // Holds a collection of XYSeriesRenderer and customizes the graph

    protected int channel;

    protected int width;


    public lineGraph(int Channel, int SampleRate)
    {
        channel=Channel;

    }

    public GraphicalView getmChartView() {
        return mChartView;
    }

    public XYMultipleSeriesDataset getmDataset() {
        return mDataset;
    }

    public XYMultipleSeriesRenderer getmRenderer() {
        return mRenderer;
    }

    public void setmChartView(GraphicalView mChartView) {
        this.mChartView = mChartView;
    }

    public void chartRepaint(){

        mChartView.repaint();

    }

    public void clearChart(){
        int i;
        for (i = 0; i < channel; i++) {
            mDataset.getSeriesAt(i).clear();
        }
        time = 0;
    }


}
