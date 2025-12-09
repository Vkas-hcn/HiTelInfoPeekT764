package com.endless.boundaries

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.endless.boundaries.databinding.FragmentDeviceBinding
import androidx.core.graphics.toColorInt

class DeviceFragment : Fragment() {

    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        // Set phone model
        binding.tvPhoneModel.text = "${Build.MANUFACTURER} ${Build.MODEL}"

        // Get device info
        val deviceInfoList = mutableListOf<Pair<String, String>>()
        
        deviceInfoList.add(Pair("Model", Build.MODEL))
        deviceInfoList.add(Pair("Screen Denstity", "${getScreenDensity()} DPI"))
        deviceInfoList.add(Pair("Screen Multiple-touch Support", "Supported"))
        deviceInfoList.add(Pair("Screen Resolution", getScreenResolution()))
        deviceInfoList.add(Pair("Manufacturer", Build.MANUFACTURER))
        deviceInfoList.add(Pair("Brand", Build.BRAND))
        deviceInfoList.add(Pair("Device", Build.DEVICE))
        deviceInfoList.add(Pair("Hardware", Build.HARDWARE))
        deviceInfoList.add(Pair("Product", Build.PRODUCT))
        deviceInfoList.add(Pair("Board", Build.BOARD))

        // Add info items to layout
        deviceInfoList.forEachIndexed { index, pair ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_device_info, binding.llDeviceInfoList, false)
            
            itemView.findViewById<android.widget.TextView>(R.id.tvLabel).text = pair.first
            itemView.findViewById<android.widget.TextView>(R.id.tvValue).text = pair.second
            
            binding.llDeviceInfoList.addView(itemView)
            
            // Add divider if not last item
            if (index < deviceInfoList.size - 1) {
                val divider = View(context)
                val layoutParams = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                )
                layoutParams.setMargins(dpToPx(16), 0, dpToPx(16), 0)
                divider.layoutParams = layoutParams
                divider.setBackgroundColor("#FFE5E7EB".toColorInt())
                binding.llDeviceInfoList.addView(divider)
            }
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun getScreenDensity(): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.densityDpi
    }

    private fun getScreenResolution(): String {
        val displayMetrics = DisplayMetrics()
        val windowManager = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return "${displayMetrics.widthPixels} x${displayMetrics.heightPixels}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
