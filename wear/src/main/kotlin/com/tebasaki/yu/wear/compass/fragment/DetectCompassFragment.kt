package com.tebasaki.yu.wear.compass.fragment

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log


public class DetectCompassFragment : Fragment() {

    private var mCallback : OnCompassListener? = null
    private var mSensorManager : SensorManager? = null
    private var mAccelSensor : Sensor? = null
    private var mMagneticSensor : Sensor? = null
    private var mSensorEventListener : SensorEventListener? = null

    private var mAccel : FloatArray = FloatArray(3)
    private var mGeomagnetic : FloatArray = FloatArray(3)
    private var azimuth : Float = 0f
    private var currentAzimuth: Float = 0f

    companion object {
        fun newInstance() : DetectCompassFragment {
            return DetectCompassFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // get SensorManager
        mSensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mMagneticSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        // implements SensorEventListener
        mSensorEventListener = object:SensorEventListener {

            override fun onSensorChanged(event: SensorEvent) {
                getAzimuth(event)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // no-op
            }

        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            mCallback = activity as OnCompassListener
        } catch(ex: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement OnCompassListener")
        }
    }

    fun getAzimuth(event: SensorEvent) {

        val alpha : Float = 0.97f

        if (Sensor.TYPE_ACCELEROMETER == event.sensor.type) {
            mAccel[0] = alpha * mAccel[0] + (1 - alpha) * event.values[0];
            mAccel[1] = alpha * mAccel[1] + (1 - alpha) * event.values[1];
            mAccel[2] = alpha * mAccel[2] + (1 - alpha) * event.values[2];
        }

        if (Sensor.TYPE_MAGNETIC_FIELD == event.sensor.type) {
            mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0];
            mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1];
            mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2];
        }

        var r : FloatArray = FloatArray(9)
        var i : FloatArray = FloatArray(9)
        var success : Boolean = SensorManager.getRotationMatrix(r, i, mAccel, mGeomagnetic);

        if (success) {
            var orientation : FloatArray = FloatArray(3)
            SensorManager.getOrientation(r, orientation);
            azimuth = Math.toDegrees(orientation.get(0).toDouble()).toFloat();
            azimuth = (azimuth + 360) % 360;

            Log.d("DetectCompassFragment", "currectAzimuth = " + currentAzimuth)
            Log.d("DetectCompassFragment", "azimuth = " + azimuth)
            mCallback?.onCompassChanged(currentAzimuth, azimuth)

            currentAzimuth = azimuth

        }
    }

    override fun onStart() {
        super.onStart()
        mSensorManager?.registerListener(mSensorEventListener, mAccelSensor, SensorManager.SENSOR_DELAY_GAME)
        mSensorManager?.registerListener(mSensorEventListener, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onStop() {
        super.onStop()
        mSensorManager?.unregisterListener(mSensorEventListener)
    }

    public interface OnCompassListener {
        fun onCompassChanged(fromAzimuth: Float, toAzimuth: Float)
    }
}