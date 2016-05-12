package com.quki.alphachair.alphachairandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1001;
    private Button onButton,offButton,receiveButton;
    private TextView sensorView;
    private BluetoothSocket mSocket = null;
    private BluetoothDevice mArduino = null;
    private static final int REQUEST_CONNECT_ARDUINO = 0;
    private static final String REQUEST_PROPELLER_ON = "1";
    private static final String REQUEST_PROPELLER_OFF = "2";
    private static final String REQUEST_FORCE_SENSOR_ON = "FSR ON";
    private static final String REQUEST_FORCE_SENSOR_OFF = "FSR OFF";

    private Thread mWorkerThread = null;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    InputStream mInputStream;
    OutputStream mOutputStream;
    String mStrDelimiter = "\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        onButton = (Button) findViewById(R.id.onButton);
        offButton = (Button) findViewById(R.id.offButton);
        sensorView = (TextView) findViewById(R.id.sensorView);
        receiveButton = (Button) findViewById(R.id.receiveButton);
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mArduino!=null){
                    writeToArduino(REQUEST_PROPELLER_ON);
                    Toast.makeText(getApplicationContext(),"프로펠러 ON",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"아두이노와 페어링을 확인하세요",Toast.LENGTH_SHORT).show();
                }
            }
        });
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mArduino!=null){
                    writeToArduino(REQUEST_PROPELLER_OFF);
                    Toast.makeText(getApplicationContext(),"프로펠러 OFF",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"아두이노와 페어링을 확인하세요",Toast.LENGTH_SHORT).show();
                }
            }
        });
        receiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonStatus = receiveButton.getText().toString();
                if(buttonStatus.equals("On")){
                    Toast.makeText(getApplicationContext(),"수신상태 On",Toast.LENGTH_SHORT).show();
                    receiveButton.setText("Off");
                    onReadyToReceiveFSR();
                }else{
                    Toast.makeText(getApplicationContext(),"수신상태 Off",Toast.LENGTH_SHORT).show();
                    receiveButton.setText("On");
                    offFSR();
                }

            }
        });
        initBluetooth();
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                findArduino();
            }
        }

    }

    /**
     * Find Arduino(HC-06)
     */
    private void findArduino() {
        Set<BluetoothDevice> mBondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bondedDevice : mBondedDevices) {
            String deviceName = bondedDevice.getName();
            if (deviceName.equals("HC-06")) {
                mArduino = bondedDevice;
                connectWithArduino(mArduino);
                break;
            }
        }
    }

    /**
     * Connect Arduino (HC-06 already found) and make socket and stream(I/O)
     * @param mArduino
     */
    private void connectWithArduino(BluetoothDevice mArduino) {

        final UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            mSocket = mArduino.createRfcommSocketToServiceRecord(SPP_UUID);
            mSocket.connect();
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            Toast.makeText(getApplicationContext(), "아두이노와 연결되었습니다.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("==SocketError==", e.toString());
            Toast.makeText(getApplicationContext(), "아두이노와 연결오류", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean writeToArduino(String msg) {
        msg += mStrDelimiter;  // 문자열 종료표시 (\n)
        try{
            // getBytes() : String을 byte로 변환
            // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
            mOutputStream.write(msg.getBytes());
            return true;
        }catch(Exception e) {
            Toast.makeText(getApplicationContext(), "전송에러: " +e.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
    }
    private void onReadyToReceiveFSR(){
        if(writeToArduino(REQUEST_FORCE_SENSOR_ON)){

            beginListenForData();
        }
    }

    private void offFSR(){
        if(writeToArduino(REQUEST_FORCE_SENSOR_OFF)){
            mWorkerThread.interrupt();
            mWorkerThread = null;
        }
    }

    private void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        mWorkerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d("===FETCHED DATA===", data);
                                            sensorView.setText(data);
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        mWorkerThread.start();
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
        if(mWorkerThread !=null){
            mWorkerThread.interrupt();
            stopWorker = true;
            mWorkerThread=null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                findArduino();
            } else {
                Toast.makeText(getApplicationContext(), "블루투스를 반드시 켜주세요", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
