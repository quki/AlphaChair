package com.quki.alphachair.alphachairandroid.bluetooth;

/**
 * Created by quki on 2016-05-12.
 */
public interface BluetoothAction {

    void connectionSuccess();
    void connectionFail();
    void setFSRDataToUI(String data);
}
