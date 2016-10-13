package com.preethi.android.crustappandroid;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.Toast;

/**
 * Created by preethi on 10/10/16.
 */

public class ShakeService extends Service implements Shaker.OnShakeListener {


    private Shaker mShaker;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {

        super.onCreate();
        this.mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        this.mAccelerometer = this.mSensorManager.getDefaultSensor(1);
        mShaker = new Shaker(this);
        mShaker.setOnShakeListener(this);
        Toast.makeText(this,"Shake Detected",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onShake() {
        Intent intent = new Intent(getApplicationContext(), com.preethi.android.crustappandroid.MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }
}