package learning.android.sensorsurvey

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val layout = inflater.inflate(R.layout.fragment_list, container, false)

        val sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        val sensorText = StringBuilder()
        for (currentSensor in sensorList) {
            sensorText.append(currentSensor.name)
                .append(System.getProperty("line.separator"))
        }
        layout.findViewById<TextView>(R.id.sensor_list).apply {
            text = sensorText.toString()
        }

        return layout
    }

    companion object {
        @JvmStatic
        fun newInstance() = ListFragment()
    }
}