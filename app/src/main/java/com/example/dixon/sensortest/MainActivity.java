package com.example.dixon.sensortest;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private SensorManager sm;
    //需要两个Sensor
    private Sensor aSensor;
    private Sensor mSensor;

    private static final int SENSOR_DELAY = 60000;
    //private static final int MAX_SENSOR_DELAY = SENSOR_DELAY * 3;

    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    private static final String TAG = "sensor";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        aSensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensor = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sm.registerListener(myListener, aSensor, SENSOR_DELAY);
        sm.registerListener(myListener, mSensor, SENSOR_DELAY);
        //更新显示数据的方法
        calculateOrientation();

    }
    //再次强调：注意activity暂停的时候释放
    public void onPause(){
        sm.unregisterListener(myListener);
        super.onPause();
    }

    public void onResume(){
        sm.registerListener(myListener, aSensor, SENSOR_DELAY);
        sm.registerListener(myListener, mSensor, SENSOR_DELAY);
        super.onResume();
    }


    final SensorEventListener myListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = sensorEvent.values;
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = sensorEvent.values;
            calculateOrientation();
        }
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            TextView tv;
            if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                tv = (TextView) findViewById(R.id.ma);
            }
            else {
                tv = (TextView) findViewById(R.id.aa);
            }
            switch (accuracy) {
                case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                    tv.setText("High");
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                    tv.setText("Medium");
                    break;
                case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                    tv.setText("Low");
                    break;
                case SensorManager.SENSOR_STATUS_UNRELIABLE:
                    tv.setText("shit");
                    break;
                default:
                    tv.setText("dunno");
            }
        }
    };

    private  void calculateOrientation() {
        TextView[] tvOri = {(TextView) findViewById(R.id.azi),
                (TextView) findViewById(R.id.pit),
                (TextView) findViewById(R.id.rol)};
        TextView tvInc = (TextView) findViewById(R.id.inc);
        TextView tvCal = (TextView) findViewById(R.id.cal);
        TextView tvSum = (TextView) findViewById(R.id.sum);
        TextView tvDif = (TextView) findViewById(R.id.dif);
        float[] orientations = new float[3];
        float inclination;
        double calInc;
        double sum;
        double dif;
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, orientations);
        inclination = SensorManager.getInclination(R);
        calInc = calInclination(orientations[1], orientations[2]);
        sum = Math.toDegrees(inclination) + Math.toDegrees(calInc);
        dif = Math.toDegrees(inclination) - Math.toDegrees(calInc);

        // 要经过一次数据格式的转换，转换为度
        DecimalFormat df = new DecimalFormat("0");
        float deg;
        for (int i = 0; i < 3; i++){
            deg = (float) Math.toDegrees(orientations[i]);
            tvOri[i].setText(df.format(deg));
        }
        deg = (float) Math.toDegrees(inclination);
        tvInc.setText(df.format(deg));
        deg = (float) Math.toDegrees(calInc);
        tvCal.setText(df.format(deg));
        tvSum.setText(df.format(sum));
        tvDif.setText(df.format(dif));
    }

    private double calInclination(double pitch, double roll){
        Log.i(TAG + "/pitch", String.valueOf(Math.toDegrees(pitch)));
        Log.i(TAG + "/roll", String.valueOf(Math.toDegrees(roll)));
        double a = Math.pow(Math.cos(pitch), 2);
        double b = Math.pow(Math.sin(roll) * Math.sin(pitch), 2);
        Log.i(TAG + "/a", String.valueOf(a));
        Log.i(TAG + "/b", String.valueOf(b));
        Log.i(TAG + "/c", String.valueOf(Math.toDegrees(Math.acos(Math.sqrt(a + b)))));
        return Math.acos(Math.sqrt(a + b));
    }
}
