package com.endless.boundaries

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.endless.boundaries.databinding.FragmentCpuBinding
import java.io.BufferedReader
import java.io.FileReader
import androidx.core.graphics.toColorInt

class CpuFragment : Fragment() {

    private var _binding: FragmentCpuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCpuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCpuInfo()
    }

    private fun loadCpuInfo() {
        // Set processor name
        binding.tvProcessorName.text = getCpuName()

        // Get CPU info
        val cpuInfoList = mutableListOf<Pair<String, String>>()
        
        cpuInfoList.add(Pair("CPU", Build.HARDWARE))
        cpuInfoList.add(Pair("Vendor", getCpuVendor()))
        cpuInfoList.add(Pair("Cores", getCpuCores()))
        cpuInfoList.add(Pair("Architecture", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"))
        cpuInfoList.add(Pair("CPU Model", Build.HARDWARE))
        cpuInfoList.add(Pair("Processor", getCpuName()))
        cpuInfoList.add(Pair("Supported ABI", Build.SUPPORTED_ABIS.joinToString(", ")))
        cpuInfoList.add(Pair("64-Bit Support", if (Build.SUPPORTED_64_BIT_ABIS.isNotEmpty()) "Yes" else "No"))
        cpuInfoList.add(Pair("Min Frequency", "${getCpuMinFreq()} MHz"))
        cpuInfoList.add(Pair("Max Frequency", "${getCpuMaxFreq()} MHz"))

        // Add info items to layout
        cpuInfoList.forEachIndexed { index, pair ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_device_info, binding.llCpuInfoList, false)
            
            itemView.findViewById<android.widget.TextView>(R.id.tvLabel).text = pair.first
            itemView.findViewById<android.widget.TextView>(R.id.tvValue).text = pair.second
            
            binding.llCpuInfoList.addView(itemView)
            
            // Add divider if not last item
            if (index < cpuInfoList.size - 1) {
                val divider = View(context)
                val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                )
                layoutParams.setMargins(dpToPx(16), 0, dpToPx(16), 0)
                divider.layoutParams = layoutParams
                divider.setBackgroundColor("#FFF0F0F0".toColorInt())
                binding.llCpuInfoList.addView(divider)
            }
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
    private fun getCpuName(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("Hardware")) {
                    return line.substring(line.indexOf(":") + 1).trim()
                }
            }
            Build.HARDWARE
        } catch (e: Exception) {
            Build.HARDWARE
        }
    }

    private fun getCpuVendor(): String {
        return try {
            val reader = BufferedReader(FileReader("/proc/cpuinfo"))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line!!.startsWith("CPU implementer") || line!!.startsWith("vendor_id")) {
                    return "Qualcomm" // Default for most Android devices
                }
            }
            "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getCpuCores(): String {
        return Runtime.getRuntime().availableProcessors().toString()
    }

    private fun getCpuMinFreq(): String {
        return try {
            val reader = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"))
            val freq = reader.readLine().toLong() / 1000
            reader.close()
            freq.toString()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getCpuMaxFreq(): String {
        return try {
            val reader = BufferedReader(FileReader("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"))
            val freq = reader.readLine().toLong() / 1000
            reader.close()
            freq.toString()
        } catch (e: Exception) {
            "Unknown"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
