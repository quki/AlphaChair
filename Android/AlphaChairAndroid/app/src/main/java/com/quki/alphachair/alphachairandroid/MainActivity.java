package com.quki.alphachair.alphachairandroid;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quki.alphachair.alphachairandroid.bluetooth.BluetoothAction;
import com.quki.alphachair.alphachairandroid.bluetooth.BluetoothConfig;
import com.quki.alphachair.alphachairandroid.bluetooth.BluetoothHelper;
import com.quki.alphachair.alphachairandroid.service.MainService;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1001;
    private Button onButton,offButton,receiveFSRButton,plusTemp,minusTemp,nextActivity;
    private TextView sensorView,temperature;
    private BluetoothHelper mBluetoothHelper;
    private static int TEMPERATURE_OFFSET = 10;
    private int mTemperature = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onButton = (Button) findViewById(R.id.onButton);
        offButton = (Button) findViewById(R.id.offButton);
        sensorView = (TextView) findViewById(R.id.sensorView);
        receiveFSRButton = (Button) findViewById(R.id.receiveFSRButton);
        plusTemp = (Button) findViewById(R.id.plusTemp);
        minusTemp = (Button) findViewById(R.id.minusTemp);
        temperature = (TextView) findViewById(R.id.temperature);
        nextActivity = (Button) findViewById(R.id.nextActivity);

        /*if(isMyServiceRunning(MainService.class))
            receiveFSRButton.setText("Off");*/

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_PROPELLER_ON);
            }
        });
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_PROPELLER_OFF);
            }
        });
        receiveFSRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonStatus = receiveFSRButton.getText().toString();
                if (buttonStatus.equals("On")) {
                    Toast.makeText(getApplicationContext(), "수신상태 On", Toast.LENGTH_SHORT).show();
                    receiveFSRButton.setText("Off");
                    mBluetoothHelper.onReadyToReceiveFSR();
                    // 서비스 생성
                    //startService();

                } else {
                    Toast.makeText(getApplicationContext(), "수신상태 Off", Toast.LENGTH_SHORT).show();
                    receiveFSRButton.setText("On");
                    mBluetoothHelper.offFSR();
                    // 서비스 중지
                   // stopService();
                }

            }
        });

        plusTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTemperature += TEMPERATURE_OFFSET;
                mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_TEMPERATURE_ON);
                temperature.setText(String.valueOf(mTemperature));
            }
        });

        minusTemp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                mTemperature -= TEMPERATURE_OFFSET;
                if(mTemperature>0){
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_TEMPERATURE_ON);
                }else{
                    mTemperature = 0;
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_TEMPERATURE_OFF);
                }
                temperature.setText(String.valueOf(mTemperature));
            }
        });
        nextActivity.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent nextIntent = new Intent(MainActivity.this,PostureActivity.class);
                startActivity(nextIntent);
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initBluetooth();
    }

    private void initBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothHelper = new BluetoothHelper(mBluetoothAdapter,getBluetoothAction(),getApplicationContext());
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                mBluetoothHelper.findArduino();
            }
        }
    }

    private BluetoothAction getBluetoothAction(){
        return new BluetoothAction() {
            @Override
            public void connectionSuccess() {
                Toast.makeText(getApplicationContext(),"연결 성공",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connectionFail() {
                Toast.makeText(getApplicationContext(),"연결 실패",Toast.LENGTH_SHORT).show();
                //finish();
            }

            @Override
            public void setFSRDataToUI(String data) {
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/
                sensorView.setText(data);
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothHelper.disconnectWithArduino();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                mBluetoothHelper.findArduino();
            } else {
                Toast.makeText(getApplicationContext(), "블루투스를 반드시 켜주세요", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startService(){
        Intent intentService = new Intent(this, MainService.class);
        startService(intentService);
    }


    private void stopService(){
        Intent intentService = new Intent(this, MainService.class);
        stopService(intentService);
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
