package ad

import android.app.Activity
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hightway.tell.peek.Core
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Date：2025/7/16
 * Describe:
 */

// 单聚合
class AdCenter {
    private val mPAH = PangleAdImpl()// 高价值
    private val mPangleAdImpl = PangleAdImpl("1") // 低价值
    private var idH = ""
    private var idL = ""

    fun setAdId(high: String, lowId: String) {
        idH = high
        idL = lowId
    }

    fun loadAd() {
        mPAH.lAd(idH)
        mPangleAdImpl.lAd(idL)
    }

    fun isReady(): Boolean {
        return mPAH.isReadyAd() || mPangleAdImpl.isReadyAd()
    }


    private var job: Job? = null
    fun showAd(ac: Activity) {
        AdE.sNumJump(0)
        if (ac is AppCompatActivity) {
            ac.onBackPressedDispatcher.addCallback {}
            job?.cancel()
            job = ac.lifecycleScope.launch {
                Core.pE("ad_done")
                delay(Random.nextLong(AdE.gDTime()))
                var isS = show(ac)
                if (isS.not()) {
                    isS = show(ac)
                }
                if (isS.not()) {
                    delay(500)
                    ac.finishAndRemoveTask()
                }
            }
        }
    }

    private var flag = 0
    private fun show(ac: Activity): Boolean {
        return when (flag) {
            0 -> {
                flag = 1
                mPAH.shAd(ac)
            }

            else -> {
                flag = 0
                mPangleAdImpl.shAd(ac)
            }
        }
    }
}