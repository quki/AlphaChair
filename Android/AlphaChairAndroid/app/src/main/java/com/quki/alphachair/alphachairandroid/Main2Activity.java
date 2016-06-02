package com.quki.alphachair.alphachairandroid;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.quki.alphachair.alphachairandroid.mydata.MyData;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class Main2Activity extends AppCompatActivity {
    RealmResults<MyData> realmResultsAsync;
    RealmChangeListener mRealmListener;
    TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        status = (TextView)findViewById(R.id.status);
        Realm realm = Realm.getInstance(getApplicationContext());
        realmResultsAsync = realm.where(MyData.class).findAllAsync(); // find data asynchronous

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRealmListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                status.setText("Data from Realm\n");
                for(int i=0; i<realmResultsAsync.size();i++){
                    status.append(realmResultsAsync.get(i).getFrontRight()+" _ "+ realmResultsAsync.get(i).getNow());
                    status.append("\n");
                }
            }
        };
        realmResultsAsync.addChangeListener(mRealmListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        realmResultsAsync.removeChangeListener(mRealmListener);
    }
}
