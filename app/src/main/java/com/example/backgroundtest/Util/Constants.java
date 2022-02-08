package com.example.backgroundtest.Util;

import java.util.UUID;

public class Constants {
    // tag for log message
    public static String TAG= "ClientActivity";

    public static int REQUEST_ENABLE_BT= 1;
    // used to request fine location permission
    public static int REQUEST_FINE_LOCATION= 2;

    //// focus v3 service. refer. https://www.foc.us/bluetooth-low-energy-api
    // service and uuid
    public static String SERVICE_STRING = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static UUID UUID_TDCS_SERVICE= UUID.fromString(SERVICE_STRING);
    // command uuid
    public static String CHARACTERISTIC_COMMAND_STRING = "0000ff02-0000-1000-8000-00805f9b34fb";
    public static UUID UUID_CTRL_COMMAND = UUID.fromString( CHARACTERISTIC_COMMAND_STRING );
    // response uuid
    public static String CHARACTERISTIC_RESPONSE_STRING = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static UUID UUID_CTRL_RESPONSE = UUID.fromString( CHARACTERISTIC_RESPONSE_STRING );


    public static String CLIENT_CHARACTERISTIC_CONFIG  = "00002902-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_CTRL_COFIG = UUID.fromString( CLIENT_CHARACTERISTIC_CONFIG );

    // focus MAC address
    public final static String MAC_ADDR= "00:BA:E1:56:D4:13";
    //public final static String MAC_ADDR= "74:C7:13:6D:AC:98";
    public final static String DEVICE_NAME = "MP-BLE";

    // scan period
    public static final long SCAN_PERIOD = 5000;
}
