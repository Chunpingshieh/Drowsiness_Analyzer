package com.example.chunping_shieh.drowsiness_analyzer;

/**
 * Created by ChunPing-Shieh on 2015/9/10.
 */
public class RunTimeTest {
    int window = 1000;
    long startTime;
    long[] runTime = new long[window];
    int counter = 0;

    public void tic(){
        startTime = System.currentTimeMillis();
    }

    public void toc(){
        if(counter >= window){
            counter = 0;
        }
        runTime[counter] = System.currentTimeMillis() - startTime;
        counter++;
    }

    public long averageRunTime(){
        long avgRuntime = 0;
        for (int i = 0; i<window; i++){
            avgRuntime += runTime[i];
        }
        //avgRuntime /= window;
        return avgRuntime;
    }
}
