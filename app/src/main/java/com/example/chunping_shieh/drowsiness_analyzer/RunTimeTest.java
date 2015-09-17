package com.example.chunping_shieh.drowsiness_analyzer;

/**
 * Created by ChunPing-Shieh on 2015/9/10.
 * This class serve as a timer
 */
public class RunTimeTest {
    int window;
    long startTime = 0;
    long[] runTime;
    int counter = 0;

    public RunTimeTest(int Window) {
        window = Window;
        runTime = new long[window];
    }



    /**runtime start*/
    public void tic(){
        startTime = System.currentTimeMillis();
    }

    /**runtime ends*/
    public void toc(){
        if(counter >= window){
            counter = 0;
        }
        runTime[counter] = System.currentTimeMillis() - startTime;
        counter++;
    }

    public long tocTime(){
        return System.currentTimeMillis() - startTime;
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
