package com.endless.boundaries

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.view.View
import android.view.WindowManager
import android.app.usage.StorageStatsManager
import android.os.storage.StorageManager
import kotlin.math.max
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.endless.boundaries.databinding.ActivityMainBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Set up immersive status bar
        setupImmersiveStatusBar()

        // Get and display device information
        getDeviceInfo()
        getMemoryInfo()
        getStorageInfo()
        getBatteryInfo()
    }

    private fun setupImmersiveStatusBar() {
        // Make status bar transparent and extend content behind it
        window.apply {
            decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
            statusBarColor = android.graphics.Color.TRANSPARENT
        }

        // Adjust padding for the ScrollView content to account for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }

    private fun getDeviceInfo() {
        // Get device model
        val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        binding.tvDeviceModel.text = deviceModel
    }

    private fun getMemoryInfo() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availableRam = memoryInfo.availMem
        val usedRam = totalRam - availableRam

        // Calculate percentage
        val usedPercentage = ((usedRam.toFloat() / totalRam.toFloat()) * 100).toInt()

        // Update UI
        binding.tvRamTotal.text = formatBytes(totalRam)
        binding.tvRamUsed.text = formatBytes(usedRam)
        binding.tvRamFree.text = formatBytes(availableRam)
        binding.tvUsedPercentage.text = "$usedPercentage%"
        binding.circularProgress.progress = usedPercentage.toFloat()
    }

    private fun getStorageInfo() {
        try {
            // System Storage - 系统分区 (/system)
            val systemStat = StatFs(Environment.getRootDirectory().path)
            val systemBlockSize = systemStat.blockSizeLong
            val systemTotalBlocks = systemStat.blockCountLong
            val systemAvailableBlocks = systemStat.availableBlocksLong
            
            val systemTotalBytes = systemTotalBlocks * systemBlockSize
            val systemAvailableBytes = systemAvailableBlocks * systemBlockSize
            val systemUsedBytes = systemTotalBytes - systemAvailableBytes
            
            val systemFreeFormatted = formatStorageSize(systemAvailableBytes)
            val systemTotalFormatted = formatStorageSize(systemTotalBytes)
            val systemPercentage = ((systemUsedBytes.toFloat() / systemTotalBytes) * 100).toInt()
            
            binding.tvSystemStoragePercentage.text = "$systemPercentage%"
            binding.pbSystemStorage.progress = systemPercentage
            binding.tvSystemStorageInfo.text = "Free: ${systemFreeFormatted}, Total: ${systemTotalFormatted}"

            // Internal Storage - 内部存储/用户数据 (/data)
            val internalStat = StatFs(Environment.getDataDirectory().path)
            val internalBlockSize = internalStat.blockSizeLong
            val internalTotalBlocks = internalStat.blockCountLong
            val internalAvailableBlocks = internalStat.availableBlocksLong

            val internalTotalUserBytes = internalTotalBlocks * internalBlockSize  // 用户可见的总空间
            val internalAvailableBytes = internalAvailableBlocks * internalBlockSize  // 用户可用空间
            val actualTotalBytes = getTotalDeviceStorageAccurate()
            val internalDisplayTotalBytes = max(actualTotalBytes, internalTotalUserBytes)
            val internalDisplayUsedBytes = internalDisplayTotalBytes - internalAvailableBytes

            val internalFreeFormatted = formatStorageSize(internalAvailableBytes)
            val internalTotalFormatted = formatStorageSize(internalDisplayTotalBytes)
            val internalPercentage = ((internalDisplayUsedBytes.toFloat() / internalDisplayTotalBytes) * 100).toInt()

            binding.tvInternalStoragePercentage.text = "$internalPercentage%"
            binding.pbInternalStorage.progress = internalPercentage
            binding.tvInternalStorageInfo.text = "Free: ${internalFreeFormatted}, Total: ${internalTotalFormatted}"

        } catch (e: Exception) {
            e.printStackTrace()
            binding.tvSystemStoragePercentage.text = "0%"
            binding.tvSystemStorageInfo.text = "Free: -- GB, Total: -- GB"
            binding.tvInternalStoragePercentage.text = "0%"
            binding.tvInternalStorageInfo.text = "Free: -- GB, Total: -- GB"
        }
    }

    private fun getTotalDeviceStorageAccurate(): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val storageStatsManager =
                    getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
                return storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
            }

            val internalStat = StatFs(Environment.getDataDirectory().path)

            val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong

            val storagePaths = arrayOf(
                Environment.getRootDirectory().absolutePath,      // /system
                Environment.getDataDirectory().absolutePath,      // /data
                Environment.getDownloadCacheDirectory().absolutePath // /cache
            )

            var total: Long = 0
            for (path in storagePaths) {
                val stat = StatFs(path)
                val blockSize = stat.blockSizeLong
                val blockCount = stat.blockCountLong
                total += blockSize * blockCount
            }

            val withSystemOverhead = total + (total * 0.07).toLong()

            max(internalTotal, withSystemOverhead)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val internalStat = StatFs(Environment.getDataDirectory().path)
                val internalTotal = internalStat.blockCountLong * internalStat.blockSizeLong
                internalTotal + (internalTotal * 0.12).toLong()
            } catch (innerException: Exception) {
                innerException.printStackTrace()
                0L
            }
        }
    }

    private fun formatStorageSize(bytes: Long): String {
        return when {
            bytes >= 1000L * 1000L * 1000L -> {
                val gb = bytes.toDouble() / (1000L * 1000L * 1000L)
                val formatted = if (gb >= 10.0) {
                    DecimalFormat("#").format(gb)
                } else {
                    DecimalFormat("#.#").format(gb)
                }
                "$formatted GB"
            }
            bytes >= 1000L * 1000L -> {
                val mb = bytes.toDouble() / (1000L * 1000L)
                val formatted = if (mb >= 10.0) {
                    DecimalFormat("#").format(mb)
                } else {
                    DecimalFormat("#.#").format(mb)
                }
                "$formatted MB"
            }
            else -> {
                "0 MB"
            }
        }
    }

    private fun getBatteryInfo() {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        binding.tvBatteryPercentage.text = "$batteryLevel%"
        binding.pbBattery.progress = batteryLevel

        // Get battery status for additional info
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, intentFilter)

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val statusText = if (isCharging) "Charging" else "Discharging"
        binding.tvBatteryInfo.text = "Status: $statusText, Level: $batteryLevel%"
    }

    private fun formatBytes(bytes: Long): String {
        val df = DecimalFormat("#.##")
        return when {
            bytes < 1024 -> "$bytes Bytes"
            bytes < 1024 * 1024 -> "${df.format(bytes / 1024.0)} KB"
            bytes < 1024 * 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
            else -> "${df.format(bytes / (1024.0 * 1024.0 * 1024.0))} GB"
        }
    }
}