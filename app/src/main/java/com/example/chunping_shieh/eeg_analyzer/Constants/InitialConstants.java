package com.example.chunping_shieh.eeg_analyzer.Constants;

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
    public static int RatioDataNumber = 100;
    public final static int plotTimeRes = 64;
    public final static String DeviceID = "00:1A:FF:09:0B:9D";
    private static int[] RatioRange = {15 , 25};

    public static Double [] freshHzSet = {0.5, 1.0, 2.0, 4.0};
    public static Integer [] channelSet = {128, 256, 512, 1024};


    public static int getRangeNum(int i){
        return (int)(InitialConstants.RatioRange[i]  / InitialConstants.FreshHz);
    }

}
