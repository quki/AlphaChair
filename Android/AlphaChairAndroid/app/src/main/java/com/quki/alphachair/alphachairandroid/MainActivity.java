package com.quki.alphachair.alphachairandroid;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.quki.alphachair.alphachairandroid.bluetooth.BluetoothAction;
import com.quki.alphachair.alphachairandroid.bluetooth.BluetoothConfig;
import com.quki.alphachair.alphachairandroid.bluetooth.BluetoothHelper;
import com.quki.alphachair.alphachairandroid.service.MainService;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1001;
    private TextView temperature, postureNoti;
    private ImageView plusTemp, minusTemp;
    private Switch propellerSwitch, fsrSwitch;
    private View parentLayout;
    private BluetoothHelper mBluetoothHelper;
    private static int TEMPERATURE_OFFSET = 10;
    private int mTemperature = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        plusTemp = (ImageView) findViewById(R.id.plusTemp);
        minusTemp = (ImageView) findViewById(R.id.minusTemp);
        temperature = (TextView) findViewById(R.id.temperature);
        postureNoti = (TextView) findViewById(R.id.postureNoti);
        parentLayout = findViewById(R.id.parentLayout);
        propellerSwitch = (Switch) findViewById(R.id.propellerSwitch);
        fsrSwitch = (Switch) findViewById(R.id.fsrSwitch);

        plusTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTemperature <= 50) {
                    mTemperature += TEMPERATURE_OFFSET;
                } else {
                    Snackbar.make(parentLayout, "온도가 너무 높습니다.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                    plusTemp.setEnabled(false);
                }

                mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_TEMPERATURE_ON);
                temperature.setText(String.valueOf(mTemperature));
            }
        });

        minusTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plusTemp.setEnabled(true);
                mTemperature -= TEMPERATURE_OFFSET;
                if (mTemperature > 0) {
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_TEMPERATURE_ON);
                } else {
                    mTemperature = 0;
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_TEMPERATURE_OFF);
                }
                temperature.setText(String.valueOf(mTemperature));
            }
        });

        propellerSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_PROPELLER_ON);
                } else {
                    mBluetoothHelper.writeToArduino(BluetoothConfig.REQUEST_PROPELLER_OFF);
                }
            }
        });
        fsrSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Snackbar.make(parentLayout, "자세를 측정합니다. 정자세를 유지해주세요...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    postureNoti.setText("자세 측정 중...");
                    mBluetoothHelper.onReadyToReceiveFSR();
                } else {
                    mBluetoothHelper.offFSR();
                    postureNoti.setText("");
                }
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
        mBluetoothHelper = new BluetoothHelper(mBluetoothAdapter, getBluetoothAction(), getApplicationContext());
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                mBluetoothHelper.findArduino();
            }
        }
    }

    private BluetoothAction getBluetoothAction() {
        return new BluetoothAction() {
            @Override
            public void connectionSuccess() {
            }

            @Override
            public void connectionFail() {
                Toast.makeText(getApplicationContext(), "연결 실패", Toast.LENGTH_SHORT).show();
                propellerSwitch.setChecked(false);
                fsrSwitch.setChecked(false);
                postureNoti.setText("");
                //finish();
            }

            @Override
            public void setFSRDataToUI(String msg) {
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });*/
                postureNoti.setText(msg);
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
                Toast.makeText(getApplicationContext(), "블루투스를 반드시 켜주세요.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startService() {
        Intent intentService = new Intent(this, MainService.class);
        startService(intentService);
    }


    private void stopService() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.myPostureData:
                Intent nextIntent = new Intent(MainActivity.this, PostureActivity.class);
                startActivity(nextIntent);
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
