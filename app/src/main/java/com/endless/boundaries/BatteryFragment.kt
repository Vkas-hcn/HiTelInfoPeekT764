package com.endless.boundaries

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.endless.boundaries.databinding.FragmentBatteryBinding
import androidx.core.graphics.toColorInt

class BatteryFragment : Fragment() {

    private var _binding: FragmentBatteryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBatteryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBatteryInfo()
    }

    private fun loadBatteryInfo() {
        val batteryManager = requireContext().getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = requireContext().registerReceiver(null, intentFilter)

        // Get battery info
        val batteryInfoList = mutableListOf<Pair<String, String>>()
        
        val level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        batteryInfoList.add(Pair("Level", "$level %"))

        val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val healthStr = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }
        batteryInfoList.add(Pair("Health", healthStr))

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val statusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
            else -> "Unknown"
        }
        batteryInfoList.add(Pair("Status", statusStr))

        val plugged = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val powerSource = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
            else -> "Battery"
        }
        batteryInfoList.add(Pair("Power source", powerSource))

        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"
        batteryInfoList.add(Pair("Technology", technology))

        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempCelsius = temperature / 10.0
        batteryInfoList.add(Pair("Temperature", String.format("%.1f°C", tempCelsius)))

        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        batteryInfoList.add(Pair("Voltage", "$voltage mV"))

        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        if (capacity > 0) {
            batteryInfoList.add(Pair("Capacity", "${capacity / 1000} mAh"))
        }

        // Add info items to layout
        batteryInfoList.forEachIndexed { index, pair ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_device_info, binding.llBatteryInfoList, false)
            
            itemView.findViewById<android.widget.TextView>(R.id.tvLabel).text = pair.first
            itemView.findViewById<android.widget.TextView>(R.id.tvValue).text = pair.second
            
            binding.llBatteryInfoList.addView(itemView)
            
            // Add divider if not last item
            if (index < batteryInfoList.size - 1) {
                val divider = View(context)
                val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                )
                layoutParams.setMargins(dpToPx(16), 0, dpToPx(16), 0)
                divider.layoutParams = layoutParams
                divider.setBackgroundColor("#FFF0F0F0".toColorInt())
                binding.llBatteryInfoList.addView(divider)
            }
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
