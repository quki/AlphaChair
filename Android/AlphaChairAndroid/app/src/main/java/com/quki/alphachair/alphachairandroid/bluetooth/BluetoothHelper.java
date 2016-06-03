package com.quki.alphachair.alphachairandroid.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.quki.alphachair.alphachairandroid.mydata.MyData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import io.realm.Realm;

/**
 * Created by quki on 2016-05-07.
 */
public class BluetoothHelper{

    private Thread mWorkerThread = null;
    private byte[] readBuffer;
    private int readBufferPosition;
    private volatile boolean stopWorker;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private String mStrDelimiter = "\n";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private BluetoothDevice mArduino;
    private BluetoothAction mBluetoothAction;
    private Context mContext;
    private Realm realm;

    public BluetoothHelper(BluetoothAdapter mBluetoothAdapter, BluetoothAction mBluetoothAction,Context mContext) {
        this.mBluetoothAdapter = mBluetoothAdapter;
        this.mBluetoothAction = mBluetoothAction;
        this.mContext = mContext;
    }

    /**
     * Find Arduino(HC-06)
     */
    public void findArduino() {
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
     *
     * @param mArduino
     */
    public void connectWithArduino(BluetoothDevice mArduino) {

        final UUID SPP_UUID = java.util.UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            mSocket = mArduino.createRfcommSocketToServiceRecord(SPP_UUID);
            mSocket.connect();
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
            mBluetoothAction.connectionSuccess();
        } catch (IOException e) {
            Log.e("==SocketError==", e.toString());
            mBluetoothAction.connectionFail();
        }
    }

    public void disconnectWithArduino() {
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean writeToArduino(String msg) {
        msg += mStrDelimiter;  // 문자열 종료표시 (\n)
        try {
            // getBytes() : String을 byte로 변환
            // OutputStream.write : 데이터를 쓸때는 write(byte[]) 메소드를 사용함. byte[] 안에 있는 데이터를 한번에 기록해 준다.
            //mOutputStream.write(msg.getBytes());
            mOutputStream.write(msg.getBytes());
            mBluetoothAction.setFSRDataToUI(msg);
            return true;
        } catch (Exception e) {
            mBluetoothAction.connectionFail();
            return false;
        }
    }

    public void onReadyToReceiveFSR() {
        if (writeToArduino(BluetoothConfig.REQUEST_FORCE_SENSOR_ON)) {
            //realm = Realm.getInstance(mContext);
            beginListenForData();
        }
    }

    public void offFSR() {
        if (writeToArduino(BluetoothConfig.REQUEST_FORCE_SENSOR_OFF)) {
            mWorkerThread.interrupt();
            stopWorker = true;
            mWorkerThread = null;
        }
    }

    public void beginListenForData() {
        final Handler handler = new Handler(); //Inform to the Main Thread
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
                                            mBluetoothAction.setFSRDataToUI(data);
                                            /*
                                            MyData mData = new MyData();
                                            mData.setName("posture");
                                            mData.setNow(new Date());
                                            mData.setPosture(data);
                                            realm.beginTransaction();
                                            realm.copyToRealm(mData);
                                            realm.commitTransaction();
                                            realm.close();*/
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
    public void test(){
        Log.e("==TEST==","delivered BTHELPER");
        MyData mData = new MyData();
        mData.setName("posture");
        mData.setNow(new Date());
        realm.beginTransaction();
        realm.copyToRealm(mData);
        realm.commitTransaction();
    }


}
