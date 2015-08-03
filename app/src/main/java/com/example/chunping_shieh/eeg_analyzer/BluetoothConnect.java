package com.example.chunping_shieh.eeg_analyzer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.chunping_shieh.eeg_analyzer.Constants.InitialConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by ChunPing-Shieh on 2015/7/8.
 */
public class BluetoothConnect {

    //BT connect stuff==========================================
    private static BluetoothAdapter btAdapter;
    private static final UUID btUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothDevice btDevice;
    public static BluetoothSocket btSocket;
    public static InputStream btInStream;
    public static OutputStream btOutStream;

    //public static boolean btState;


    //BT connecting functions=======================================================================
    public static String startBT() {
        //This function try to connect the BT Device and return the status of the connection
        String connectStatus = enableBT();
        if(connectStatus == "Start!")
            return connectStatus;
        btAdapter.cancelDiscovery();
        return connectStatus;
    }
    private static String enableBT() {
        //This function walk through the steps of enabling the BT and return the status of the connection
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null){
            return "device has no bluetooth";
        }

        if (!btAdapter.isEnabled()){
            return "Turn on the Bluetooth!";
        }

        BluetoothDevice tmpBTDevice = btAdapter.getRemoteDevice(InitialConstants.DeviceID);




        if(tmpBTDevice == null){
            return "Can't find Device";

        }else{
            btDevice = tmpBTDevice;
        }

        return "Start!";
    }
    public static boolean connectBT(){
        //This function tries to connect the BT device and return the status by text
        try {
            btSocket = btDevice.createRfcommSocketToServiceRecord(btUUID);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            btOutStream = btSocket.getOutputStream();
            btOutStream.write(BRICommandSet.parseStartCommand());
        } catch (IOException e) {
            e.printStackTrace();
            //BRICommandSet.releaseAllResources();
            return false;
        }
        try {
            btInStream = btSocket.getInputStream();
        } catch (IOException e) {
            return false;
        }


        return true;
    }

    //Send BRI Command
    public static void ControlNotchFilter(){
        try {
            if (InitialConstants.NotchFilterOn)btOutStream.write(BRICommandSet.NotchFilter_on());
            else btOutStream.write(BRICommandSet.NotchFilter_off());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
