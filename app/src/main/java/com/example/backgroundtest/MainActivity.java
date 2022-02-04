package com.example.backgroundtest;

import static com.example.backgroundtest.Util.Constants.REQUEST_FINE_LOCATION;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.backgroundtest.Util.BluetoothConnectManager;

public class MainActivity extends AppCompatActivity {
    private BluetoothConnectManager blueInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startBtnBtn = findViewById(R.id.startBtn);
        Button stopBtnBtn = findViewById(R.id.stopBtn);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
//                builder.setMessage("어플리케이션이 블루투스를 감지 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
//                builder.setPositiveButton(android.R.string.ok, null);
//
//                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, 2 );
//                    }
//                });
//                builder.show();
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (this.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//                builder.setTitle("블루투스에 대한 액세스가 필요합니다");
//                builder.setMessage("어플리케이션이 블루투스를 연결 할 수 있도록 위치 정보 액세스 권한을 부여하십시오.");
//                builder.setPositiveButton(android.R.string.ok, null);
//
//                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
//
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 3 );
//                    }
//                });
//                builder.show();
//            }
//        }


        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();

            Log.d("bluetooth", "스캔 실패 : 권한 허용이 안되어 있음");
            return;
        }

        startBtnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermission();

                    Toast.makeText(getApplicationContext(), "블루투스 권한을 연결해 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(getApplicationContext(), "서비스 시작", Toast.LENGTH_SHORT).show();

                blueInstance = BluetoothConnectManager.getInstance();
                blueInstance.setViewContext(getApplicationContext());
                blueInstance.initBluetooth( (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE) ); // bluetoothManager

                Intent intent = new Intent(MainActivity.this, MyService.class);
                startService(intent);

//                String[] LIST_MENU = {"LIST1", "LIST2", "LIST3"} ;
//                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, LIST_MENU ) ;
//
//                ListView listview = (ListView) findViewById(R.id.listview1) ;
//                listview.setAdapter(adapter) ;
//                listview.setVisibility(View.VISIBLE);

                /*
                String url = "my://app";
                Intent intent = new Intent(Intent.ACTION_MAIN, Uri.parse(url));
                intent.putExtra("yju_text", "testText");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                이렇게 하거나

                String url = "my://app?yju_text=testText";
                Intent intent = new Intent(Intent.ACTION_MAIN, Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                이렇게 하거나

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "testText");
                startActivity(intent);

                */
            }
        });

        stopBtnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "서비스 중지", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, MyService.class);
                stopService(intent);

                blueInstance.disconnectGattServer();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // finish app if the BLE is not supported
        if( !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE ) ) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent(MainActivity.this, MyService.class);
        stopService(intent);

        blueInstance.disconnectGattServer();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        requestPermissions( new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION );
    }

}