package ad

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.appsflyer.AFAdRevenueData
import com.appsflyer.AdRevenueScheme
import com.appsflyer.AppsFlyerLib
import com.appsflyer.MediationNetwork
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionCallback
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadCallback
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
import com.bytedance.sdk.openadsdk.api.model.PAGAdEcpmInfo
import com.bytedance.sdk.openadsdk.api.model.PAGErrorModel
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.hightway.tell.peek.Core
import org.json.JSONObject
import java.util.Currency


/**
 * Date：2025/7/10
 * Describe:
 */
class PangleAdImpl(val t: String = "") {
    private var isL = false
    private var lT = 0L
    private var mAd: PAGInterstitialAd? = null
    var loadTime = 0L
    private var mId = ""

    fun lAd(id: String) {
        if (id.isBlank()) return
        if (isL && System.currentTimeMillis() - lT < 61000) return
        if (mAd != null) return
        if (AdE.isLoadLi()) {
            if (!AdE.isLoad) {
                AdE.isLoad = true
                Core.pE("advertise_load_limit$t")
            }
            return
        }
        mId = id
        isL = true
        lT = System.currentTimeMillis()
        Core.pE("advertise_req$t")
        AdE.adLoad()
        PAGInterstitialAd.loadAd(
            id,
            PAGInterstitialRequest(Core.mApp),
            object : PAGInterstitialAdLoadCallback {
                override fun onError(pagErrorModel: PAGErrorModel) {
                    isL = false
                    Core.pE(
                        "advertise_fail$t",
                        "${pagErrorModel.errorCode}_${pagErrorModel.errorMessage}"
                    )
                }

                override fun onAdLoaded(pagInterstitialAd: PAGInterstitialAd) {
                    mAd = pagInterstitialAd
                    isL = false
                    Core.pE("advertise_get$t")
                    loadTime = System.currentTimeMillis()
                    AdE.adLoadSuccess()
                }
            })
    }

    fun isReadyAd(): Boolean {
        return mAd != null
    }

    fun shAd(a: Activity): Boolean {
        val ad = mAd
        if (ad != null) {
            val time = System.currentTimeMillis()
            Core.pE("advertise_show")
            ad.setAdInteractionCallback(object : PAGInterstitialAdInteractionCallback() {
                override fun onAdReturnRevenue(pagAdEcpmInfo: PAGAdEcpmInfo?) {
                    super.onAdReturnRevenue(pagAdEcpmInfo)
                    Core.pE("advertise_show_t", "${(System.currentTimeMillis() - time) / 1000}")
                    pagAdEcpmInfo?.let {
                        val adRevenueData = AFAdRevenueData(
                            it.adnName,  // monetizationNetwork
                            MediationNetwork.CUSTOM_MEDIATION,  // mediationNetwork
                            "USD",  // currencyIso4217Code
                            it.cpm.toDouble() / 1000 // revenue
                        )
                        val additionalParameters: MutableMap<String, Any> = HashMap()
                        additionalParameters[AdRevenueScheme.COUNTRY] = it.country
                        additionalParameters[AdRevenueScheme.AD_UNIT] = it.adUnit
                        additionalParameters[AdRevenueScheme.AD_TYPE] = "i"
                        additionalParameters[AdRevenueScheme.PLACEMENT] = it.placement
                        AppsFlyerLib.getInstance().logAdRevenue(adRevenueData, additionalParameters)
                        postValue(it)
                    }
                    AdE.adShow()
                    AdE.checkAdIsReadyAndGoNext()
                }

                override fun onAdDismissed() {
                    super.onAdDismissed()
                    a.finishAndRemoveTask()
                    AdE.isSAd = false
                }

                override fun onAdShowFailed(pagErrorModel: PAGErrorModel) {
                    super.onAdShowFailed(pagErrorModel)
                    a.finishAndRemoveTask()
                    Core.pE(
                        "advertise_fail_api",
                        "${pagErrorModel.errorCode}_${pagErrorModel.errorMessage}"
                    )
                    AdE.isSAd = false
                    lAd(mId)
                    AdE.lastSAdTime = 0
                    AdE.checkAdIsReadyAndGoNext()
                }
            })
            ad.show(a)
            mAd = null
            return true
        }
        return false
    }


    private fun postValue(si: PAGAdEcpmInfo) {
        // TODO
        Log.e("TAG", "广告加载原值: ${si.cpm}")
        Core.postAd(
            JSONObject().put("pore", si.cpm.toDouble() * 1000)//ad_pre_ecpm
                .put("minimal", "USD")//currency
                .put("bade", si.adnName)//ad_network
                .put("martha", "pangle")//ad_source_client
                .put("sauterne", si.placement)//ad_code_id
                .put("eve", si.adUnit)//ad_pos_id
                .put("congest", si.adFormat)//ad_format
                .toString()
        )
        val cpm = si.cpm.toDouble() / 1000
        AdE.postEcpm(cpm)
    }

}