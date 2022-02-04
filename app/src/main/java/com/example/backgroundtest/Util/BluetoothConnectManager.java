package com.example.backgroundtest.Util;

import static com.example.backgroundtest.Util.Constants.DEVICE_NAME;
import static com.example.backgroundtest.Util.Constants.MAC_ADDR;
import static com.example.backgroundtest.Util.Constants.SCAN_PERIOD;
import static com.example.backgroundtest.Util.Constants.UUID_CTRL_COFIG;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.backgroundtest.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BluetoothConnectManager {
    private final String SEND_PAKAGE_NAME = "com.example.broadcastexample";
    private final String SEND_ACTION_NAME = "example.test.broadcast";

    private BluetoothAdapter blueAdapter;
    private BluetoothLeScanner blueScanner;
    private BluetoothGatt blueGatt;

    private Map<String, BluetoothDevice> scanList;
    private ScanCallback scan_cb_;
    private Handler scanHandler;

    private boolean isConnected = false;
    private boolean isScanning = false;

    private Context viewContext;

    public Context getViewContext() { return viewContext; }
    public Map<String, BluetoothDevice> getScanList() { return scanList; }

    public void setViewContext(Context viewContext) { this.viewContext = viewContext; }

    private BluetoothConnectManager() { }

    private static class SingletonHolder {
        public static final BluetoothConnectManager INSTANCE = new BluetoothConnectManager();
    }

    public static BluetoothConnectManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void initBluetooth(BluetoothManager blueManager) {
        blueAdapter = blueManager.getAdapter();
        blueScanner = blueAdapter.getBluetoothLeScanner();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startScan();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startScan() {
        if (blueAdapter == null || !blueAdapter.isEnabled()) {
            // 블루투스 연결이 아예 안되서 실행 후 Retuen 때리면 됌.
            Log.d("bluetooth", "스캔 실패 : 검색한 블루투스 없음");
            Toast.makeText(viewContext, "블루투스를 연결 할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        disconnectGattServer();

        List<ScanFilter> filters= new ArrayList<>();

        ScanFilter scan_filter= new ScanFilter.Builder()
                .setDeviceName(DEVICE_NAME)
                //.setServiceUuid( new ParcelUuid( UUID_TDCS_SERVICE ) )
                .build();

        filters.add( scan_filter );

        ScanSettings settings= new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_POWER )
                .build();

        scanList = new HashMap<>();
        scan_cb_ = new BLEScanCallback( scanList );

        blueScanner.startScan( filters, settings, scan_cb_ );

        isScanning = true;

        // SCAN_PERIOD ( 5초 ) 뒤에 블루투스 스캔 종료
        scanHandler = new Handler();
        scanHandler.postDelayed( this::stopScan, SCAN_PERIOD );
    }

    private void stopScan() {
        // check pre-conditions
        if( isScanning && blueAdapter != null && blueAdapter.isEnabled() && blueScanner != null ) {
            blueScanner.stopScan( scan_cb_ );
            scanComplete();
        }

        // reset flags
        scan_cb_= null;
        isScanning = false;
        scanHandler = null;

        // update the status
        Log.d("블루투스","scanning stopped" );
    }

    private void scanComplete() {
        // check if nothing found
        if( scanList.isEmpty() ) {
            Log.d( "scanComplete", "scan results is empty" );
            return;
        }

        // loop over the scan results and connect to them
        for( String device_addr : scanList.keySet() ) {
            BluetoothDevice device= scanList.get( device_addr );

            if( MAC_ADDR.equals( device_addr) ) {
                Log.d( "scanCompele", "connecting device: " + device_addr );

                connectDevice(device);
            }
        }
    }

    private void connectDevice( BluetoothDevice _device ) {
        // update the status
        //Toast.makeText(getApplicationContext(), "Connecting to " + _device.getAddress(), Toast.LENGTH_SHORT).show();

        GattClientCallback gatt_client_cb= new GattClientCallback();
        blueGatt = _device.connectGatt( getViewContext(), false, gatt_client_cb );
    }

    public void disconnectGattServer() {
        isConnected = false;

        if( blueGatt != null ) {
            blueGatt.disconnect();
            blueGatt.close();
        }
    }

    private class BLEScanCallback extends ScanCallback {
        private Map<String, BluetoothDevice> cb_scan_results_;

        /*
        Constructor
         */
        BLEScanCallback( Map<String, BluetoothDevice> _scan_results ) {
            cb_scan_results_= _scan_results;
        }

        @Override
        public void onScanResult( int _callback_type, ScanResult _result ) {
            Log.d( "BlueScanCallback", "onScanResult" );
            addScanResult( _result );
        }

        @Override
        public void onBatchScanResults( List<ScanResult> _results ) {
            for( ScanResult result: _results ) {
                addScanResult( result );
            }
        }

        @Override
        public void onScanFailed( int _error ) {
            Log.e( "BlueScanCallback", "BLE scan failed with code " +_error );
        }

        /*
        Add scan result
         */
        private void addScanResult( ScanResult _result ) {
            BluetoothDevice device= _result.getDevice();
            String device_address= device.getAddress();
            cb_scan_results_.put( device_address, device );

            Log.d( "입력값", "scan results device: " + device );
            Log.d( "입력값", "add scanned device: " + device_address );
        }
    }

    // 블루투스 연결중일때의 콜백 클래스 ( 연결중 행위에 대한 값들을 받아온다. )
    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange( BluetoothGatt _gatt, int _status, int _new_state ) {
            super.onConnectionStateChange( _gatt, _status, _new_state );
            if( _status == BluetoothGatt.GATT_FAILURE ) {
                disconnectGattServer();
                return;
            } else if( _status != BluetoothGatt.GATT_SUCCESS ) {
                disconnectGattServer();
                return;
            }

            if( _new_state == BluetoothProfile.STATE_CONNECTED ) {
                blueGatt.discoverServices();
                isConnected = true;
                Log.d( "GattClientCallback", "Connected to the GATT server" );
            } else if ( _new_state == BluetoothProfile.STATE_DISCONNECTED ) {
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered( BluetoothGatt _gatt, int _status ) {
            super.onServicesDiscovered( _gatt, _status );
            // check if the discovery failed
            if( _status != BluetoothGatt.GATT_SUCCESS ) {
                Log.e( "GattClientCallback", "Device service discovery failed, status: " + _status );
                return;
            }

            BluetoothGattCharacteristic ch = BluetoothUtils.findResponseCharacteristic(_gatt);

            _gatt.setCharacteristicNotification(ch, true);

            BluetoothGattDescriptor descriptor = ch.getDescriptor(UUID_CTRL_COFIG);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            _gatt.writeDescriptor(descriptor);

            // log for successful discovery
            Log.d( "GattClientCallback", "Services discovery is successful" );
        }

        @Override
        public void onCharacteristicChanged( BluetoothGatt _gatt, BluetoothGattCharacteristic _characteristic ) {
            super.onCharacteristicChanged( _gatt, _characteristic );

            Log.d( "GattClientCallback", "characteristic changed: " + _characteristic.getUuid().toString() );
            readCharacteristic( _characteristic );
        }

        @Override
        public void onCharacteristicWrite( BluetoothGatt _gatt, BluetoothGattCharacteristic _characteristic, int _status ) {
            super.onCharacteristicWrite( _gatt, _characteristic, _status );
            if( _status == BluetoothGatt.GATT_SUCCESS ) {
                Log.d( "GattClientCallback", "Characteristic written successfully" );
            } else {
                Log.e( "GattClientCallback", "Characteristic write unsuccessful, status: " + _status) ;
                disconnectGattServer();
            }
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d ("GattClientCallback", "Characteristic read successfully" );
                readCharacteristic(characteristic);
            } else {
                Log.e( "GattClientCallback", "Characteristic read unsuccessful, status: " + status);
                // Trying to read from the Time Characteristic? It doesnt have the property or permissions
                // set to allow this. Normally this would be an error and you would want to:
                // disconnectGattServer();
            }
        }

        /*
        Log the value of the characteristic
        @param characteristic
         */
        private void readCharacteristic( BluetoothGattCharacteristic _characteristic ) {
            byte[] data= _characteristic.getValue();

            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data) {
                    stringBuilder.append(String.format("%02X", byteChar));
                }

                String oid_input_cur = stringBuilder.toString();

                sendOID_BroadCast(oid_input_cur);

                Log.d("BluetoothConnectManager", oid_input_cur);
            }
        }
    }

    private void sendOID_BroadCast(String OID){
        if(OID.substring(10, 16).equals("00012F") && isRunningProcess(viewContext, "com.example.broadcastexample")) {
            // 앱이 켜진 상태 확인
            String url = "myapp://app";
            Intent sendIntent = new Intent(Intent.ACTION_MAIN, Uri.parse(url));
            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.setPackage(SEND_PAKAGE_NAME);
            sendIntent.putExtra("KEY", OID);

            viewContext.startActivity(sendIntent);
        }else {
            Intent sendIntent = new Intent();
            sendIntent.setPackage(SEND_PAKAGE_NAME);
            sendIntent.setAction(SEND_ACTION_NAME);
            sendIntent.putExtra("KEY", OID);

            viewContext.sendBroadcast(sendIntent);
        }
    }

    private boolean isRunningProcess(Context context, String packageName) {
        boolean isRunning = false;

        ActivityManager actMng = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = actMng.getRunningAppProcesses();

        for(ActivityManager.RunningAppProcessInfo rap : list)
        {
            if(rap.processName.equals(packageName))
            {
                isRunning = true;
                break;
            }
        }

        return isRunning;
    }
}
