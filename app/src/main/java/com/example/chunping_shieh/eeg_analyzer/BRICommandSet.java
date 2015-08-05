package com.example.chunping_shieh.eeg_analyzer;

import android.bluetooth.BluetoothSocket;

import com.example.chunping_shieh.eeg_analyzer.Constants.InitialConstants;

import java.io.IOException;

/**
 * Created by ChunPing-Shieh on 2015/7/6.
 */
public class BRICommandSet {

    private static double[] tmpData = new double[InitialConstants.Resolution/8];//tmpData holds the New Channel data of the window

    private static byte setCommand1(){
        byte command;
        int R,X;
        if (InitialConstants.Resolution == 24)
            R = 0;
        else
            R = 1;

        X = (int)(Math.log(InitialConstants.SampleRate)/Math.log(2));
        command = (byte)((R << 4) + X);
        return command;
    }

    private static byte setCommand2(){
        byte command;
        int G, Y;
        switch(InitialConstants.Gain) {
            case 6:
                G=0;
                break;
            case 8:
                G=5;
                break;
            case 12:
                G=6;
                break;
            case 16:
                G=7;
                break;
            case 32:
                G=8;
                break;
            case 64:
                G=9;
                break;
            case 128:
                G=0xA;
            default:
                G=InitialConstants.Gain;
        }

        int channel=0;
        if(InitialConstants.Channel==65){
            channel=64;
        }else{
            channel=InitialConstants.Channel;
        }

        Y = (int)(Math.log(channel)/Math.log(2));
        command = (byte)((G << 4) + Y);
        return command;
    }

    public static byte[] parseStartCommand() {
        byte [] command = new byte [5];
        command[0] = (byte)0xFE;
        command[1] = (byte)0x7;
        command[2] = setCommand1();
        command[3] = setCommand2();
        command[4] = (byte)0xFF;
        return command;
    }

    public static byte[] parseStopCommand() {
        byte [] command = new byte [5];
        command[0] = (byte)0xFE;
        command[1] = (byte)0x00;
        command[2] = setCommand1();
        command[3] = setCommand2();
        command[4] = (byte)0xFF;
        return command;
    }

    //BT reading functions========================================================================
    public static int[] read_header() throws IOException {
        int[] header = new int[2];
        //header

        for (int k = 0; k < 2; k++) header[k] =  BluetoothConnect.btInStream.read();
        return header;
    }

    public static double[] read_Channel_Data() throws IOException {
        for (int x = 0; x < tmpData.length; x++) tmpData[x] =  BluetoothConnect.btInStream.read();
        return tmpData;
    }

    public static void read_LeadOnOff() throws IOException {

        if(InitialConstants.Channel==32){
            for(int l=0;l<4;l++)BluetoothConnect.btInStream.read();

        }else if(InitialConstants.Channel==64 ||InitialConstants.Channel==65){

            for(int l=0;l<8;l++)BluetoothConnect.btInStream.read();

        }else{

            for(int l=0;l<2;l++)BluetoothConnect.btInStream.read();
        }
    }

    public static byte[] NotchFilter_on(){
        byte [] command = new byte [5];
        command[0] = (byte)0xFE;
        command[1] =(byte)0x05;
        command[2] = setCommand1();
        command[3] = setCommand2();
        command[4] = (byte)0xFF;

        return command;
    }

    public static byte[] NotchFilter_off(){
        byte [] command = new byte [5];
        command[0] = (byte)0xFE;
        command[1] =(byte)0x04;
        command[2] = setCommand1();
        command[3] = setCommand2();
        command[4] = (byte)0xFF;

        return command;
    }

    private byte[] Change_Gain(){
        byte [] command = new byte [5];
        command[0] = (byte)0xFE;
        command[1] =(byte)0x02;
        command[2] = setCommand1();
        command[3] = setCommand2();
        command[4] = (byte)0xFF;

        return command;
    }

    public static void releaseAllResources() {
        try {
            if(BluetoothConnect.btOutStream != null){
                BluetoothConnect.btOutStream.write(BRICommandSet.parseStopCommand());
                BluetoothConnect.btOutStream.close();
            }
            if (BluetoothConnect.btInStream != null)BluetoothConnect.btInStream.close();
            if (BluetoothConnect.btSocket != null)BluetoothConnect.btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
