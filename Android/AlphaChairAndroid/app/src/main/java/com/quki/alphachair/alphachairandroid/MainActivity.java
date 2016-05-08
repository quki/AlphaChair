package com.quki.alphachair.alphachairandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1001;
    private Button onButton,offButton;
    BluetoothSocket mSocket = null;
    BluetoothDevice mArduino = null;
    private static final int REQUEST_CONNECT_ARDUINO = 0;
    private static final int REQUEST_PROPELLER_ON = 1;
    private static final int REQUEST_PROPELLER_OFF = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onButton = (Button) findViewById(R.id.onButton);
        offButton = (Button) findViewById(R.id.offButton);
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mArduino!=null){
                    sendToArduino(REQUEST_PROPELLER_ON);
                }else{
                    Toast.makeText(getApplicationContext(),"아두이노와 페어링을 확인하세요",Toast.LENGTH_SHORT).show();
                }
            }
        });
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mArduino!=null){
                    sendToArduino(REQUEST_PROPELLER_OFF);
                }else{
                    Toast.makeText(getApplicationContext(),"아두이노와 페어링을 확인하세요",Toast.LENGTH_SHORT).show();
                }
            }
        });
        initBluetooth();
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "현재 블루투스가 비활성 상태입니다.", Toast.LENGTH_LONG).show();
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                findArduino();
            }
        }

    }

    private void findArduino() {

        Set<BluetoothDevice> mBondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bondedDevice : mBondedDevices) {
            String deviceName = bondedDevice.getName();
            if (deviceName.equals("HC-06")) {
                mArduino = bondedDevice;
                connectWithArduino(mArduino,REQUEST_CONNECT_ARDUINO);
                break;
            }
        }
    }

    private void connectWithArduino(BluetoothDevice mArduino,int requestCode) {
        // UUID 설정 (SPP)
        final UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            mSocket = mArduino.createRfcommSocketToServiceRecord(SPP_UUID);

        } catch (IOException e) {
            Log.e("==SocketError==", e.toString());
            return;
        }

        try {
            mSocket.connect();
            Toast.makeText(getApplicationContext(), "Connection success", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("==ConnectError==", e.toString());
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Connection Fail", Toast.LENGTH_SHORT).show();
            return;
        }

        sendToArduino(requestCode);
    }

    private void sendToArduino(int data){
        // Write the data by using OutputStreamWriter
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(mSocket.getOutputStream());
            outputStreamWriter.write(data);
            outputStreamWriter.flush();
            Toast.makeText(getApplicationContext(), "write success", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "write error", Toast.LENGTH_SHORT).show();
            Log.e("==WriteError==", e.toString());
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mSocket!=null){
            try{
                mSocket.close();
                mSocket = null;
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                findArduino();
            } else {
                Toast.makeText(getApplicationContext(), "블루투수를 반드시 켜주세요", Toast.LENGTH_LONG).show();
                finish();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
