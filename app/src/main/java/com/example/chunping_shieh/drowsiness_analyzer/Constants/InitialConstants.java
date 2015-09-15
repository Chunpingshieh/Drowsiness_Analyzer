package com.example.chunping_shieh.drowsiness_analyzer.Constants;

/**
 * Created by ChunPing-Shieh on 2015/7/6.
 */
public class InitialConstants {
    //Initial Constants==========================================
    public final static int Channel = 8;
    public final static int Gain = 1;
    public static int Resolution = 24;
    public static int SampleRate = 128;
    public static double FreshHz = 0.5;
    public static boolean NotchFilterOn = true;
    public static int[] ChannelSet = {1,2,4,8,16,32,64,128,256,512,1024,0,0,0,0,0};
    public static int RatioDataNumber = 1200;
    public final static int plotRes = 150;
    public final static int plotTimeRes = 60;
    public static int MovingAverageWindowWidth = 20;
    public final static String DeviceID = "00:1A:FF:09:0B:9D";
    private static int betaLow = 15;
    private static int betaHigh = 20;
    private static int thetaLow = 4;
    private static int thetaHigh = 7;


    public static Double [] freshHzSet = {0.5, 1.0, 2.0, 4.0};
    public static Integer [] channelSet = {128, 256, 512, 1024};


    public static int getBetaLow() {
        return (int)(betaLow / InitialConstants.FreshHz);
    }

    public static int getBetaHigh() {
        return (int)(betaHigh / InitialConstants.FreshHz);
    }

    public static int getThetaLow() {
        return (int)(thetaLow / InitialConstants.FreshHz);
    }

    public static int getThetaHigh() {
        return (int)(thetaHigh / InitialConstants.FreshHz);
    }
}
