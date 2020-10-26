package learning.android.sensorsurvey

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class DetailFragment : Fragment(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var lightSensor: Sensor? = null

    private var lightTextView: TextView? = null
    private var proximityTextView: TextView? = null

    override fun onStart() {
        super.onStart()
        if (proximitySensor != null) {
            sensorManager?.registerListener(this,
                    proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (lightSensor != null) {
            sensorManager?.registerListener(this,
                    lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStop() {
        super.onStop()
        sensorManager?.unregisterListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_detail, container, false)

        // Sensors
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        lightSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_LIGHT)

        val sensorError = resources.getString(R.string.error_no_sensor)
        lightTextView = rootView.findViewById<TextView>(R.id.label_light).apply {
            text = when (lightSensor) {
                null -> sensorError
                else -> lightSensor.toString()
            }
        }
        proximityTextView = rootView.findViewById<TextView>(R.id.label_proximity).apply {
            text = when (proximitySensor) {
                null -> sensorError
                else -> proximitySensor.toString()
            }
        }
        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance() = DetailFragment()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentValue = event?.values?.get(0) ?: 0f
        when (event?.sensor?.type) {
            Sensor.TYPE_LIGHT -> {
                lightTextView?.text = resources
                        .getString(R.string.light_label, currentValue)
                Log.d("LIGHT_SENSOR", currentValue.toString())
            }
            Sensor.TYPE_PROXIMITY -> {
                proximityTextView?.text = resources
                        .getString(R.string.proximity_label, currentValue)
                Log.d("PROXIMITY_SENSOR", currentValue.toString())
            }
            else -> {}
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }
}