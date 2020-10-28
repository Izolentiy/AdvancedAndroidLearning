/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.tiltspot;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    // Accelerometer and magnetometer sensors, as retrieved from the
    // sensor manager.
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    // TextViews to display current sensor values.
    private TextView mTextSensorAzimuth;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;

    // ImageView for spots
    private ImageView mSpotTop;
    private ImageView mSpotLeft;
    private ImageView mSpotRight;
    private ImageView mSpotBottom;

    private CalculateRotationTask mCalculationTask;

    // Variables to hold sensors' data
    float[] mAccelerometerData = new float[3];
    float[] mMagnetometerData = new float[3];
    int mDisplayRotation;

    // Very small values for the accelerometer (on all three axes) should
    // be interpreted as 0. This value is the amount of acceptable
    // non-zero drift.
    private static final float VALUE_DRIFT = 0.05f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextSensorAzimuth = findViewById(R.id.value_azimuth);
        mTextSensorPitch = findViewById(R.id.value_pitch);
        mTextSensorRoll = findViewById(R.id.value_roll);

        mSpotTop = findViewById(R.id.spot_top);
        mSpotLeft = findViewById(R.id.spot_left);
        mSpotRight = findViewById(R.id.spot_right);
        mSpotBottom = findViewById(R.id.spot_bottom);

        // Get accelerometer and magnetometer sensors from the sensor manager.
        // The getDefaultSensor() method returns null if the sensor
        // is not available on the device.
        mSensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        mDisplayRotation = ((WindowManager)getSystemService(WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();

    }

    @Override
    protected void onDestroy() {
        if (mCalculationTask != null) {
            mCalculationTask.cancel(true);
            mCalculationTask = null;
        }
        super.onDestroy();
    }

    /**
     * Listeners for the sensors are registered in this callback so that
     * they can be unregistered in onStop().
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Listeners for the sensors are registered in this callback and
        // can be unregistered in onStop().
        //
        // Check to ensure sensors are available before registering listeners.
        // Both listeners are registered with a "normal" amount of delay
        // (SENSOR_DELAY_NORMAL).
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister all sensor listeners in this callback so they don't
        // continue to use resources when the app is stopped.
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        mCalculationTask = (CalculateRotationTask)
                new CalculateRotationTask().execute(sensorEvent);
    }

    public void updateUI(CalculateRotationTask.Result result) {
        float azimuth = result.orientationValues[0];
        float pitch = result.orientationValues[1];
        float roll = result.orientationValues[2];

        mTextSensorAzimuth.setText
                (getResources().getString(R.string.value_format, azimuth));
        mTextSensorPitch.setText
                (getResources().getString(R.string.value_format, pitch));
        mTextSensorRoll.setText
                (getResources().getString(R.string.value_format, roll));

        if (Math.abs(pitch) < VALUE_DRIFT)
            pitch = 0;
        if (Math.abs(roll) < VALUE_DRIFT)
            roll = 0;

        // To prevent artifacts while quick rotation
        mSpotTop.setAlpha(0f);
        mSpotLeft.setAlpha(0f);
        mSpotRight.setAlpha(0f);
        mSpotBottom.setAlpha(0f);

        if (pitch > 0)
            mSpotBottom.setAlpha(pitch);
        else
            mSpotTop.setAlpha(Math.abs(pitch));

        if (roll > 0)
            mSpotLeft.setAlpha(roll);
        else
            mSpotRight.setAlpha(Math.abs(roll));
    }

    private class CalculateRotationTask
            extends AsyncTask<SensorEvent, Void, CalculateRotationTask.Result>{

        /**
         * Wrapper class for result
         */
        class Result {
            public float[] orientationValues;
            public Result(float[] values) {
                orientationValues = values;
            }
        }

        @Override
        protected Result doInBackground(SensorEvent... sensorEvents) {
            int sensorType = sensorEvents[0].sensor.getType();
            switch (sensorType) {
                case Sensor.TYPE_ACCELEROMETER:
                    mAccelerometerData = sensorEvents[0].values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    mMagnetometerData = sensorEvents[0].values.clone();
                    break;
            }
            float[] rotationMatrix = new float[9];
            boolean rotationOk = SensorManager.getRotationMatrix
                    (rotationMatrix, null, mAccelerometerData, mMagnetometerData);

            float[] rotationMatrixAdjusted = new float[9];
            switch (mDisplayRotation) {
                case Surface.ROTATION_0:
                    rotationMatrixAdjusted = rotationMatrix.clone();
                    break;
                case Surface.ROTATION_90:
                    SensorManager.remapCoordinateSystem(
                            rotationMatrix, SensorManager.AXIS_Y,
                            SensorManager.AXIS_MINUS_X, rotationMatrixAdjusted);
                    break;
                case Surface.ROTATION_180:
                    SensorManager.remapCoordinateSystem(
                            rotationMatrix, SensorManager.AXIS_MINUS_X,
                            SensorManager.AXIS_MINUS_Y, rotationMatrixAdjusted);
                case Surface.ROTATION_270:
                    SensorManager.remapCoordinateSystem(
                            rotationMatrix, SensorManager.AXIS_MINUS_Y,
                            SensorManager.AXIS_X, rotationMatrixAdjusted);
            }

            float[] orientationValues = new float[3];
            if (rotationOk)
                SensorManager.getOrientation(rotationMatrixAdjusted, orientationValues);
            return new Result(orientationValues);
        }

        @Override
        protected void onPostExecute(Result result) {
            if (result != null)
                updateUI(result);
        }
    }
}