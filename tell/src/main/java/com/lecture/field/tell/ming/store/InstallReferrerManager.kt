package com.lecture.field.tell.ming.store

import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.lecture.field.tell.ext.PeekExample

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import mei.ye.DataPreferences
import kotlin.coroutines.resume

// 安装来源管理类
object InstallReferrerManager {
    private var currentClient: InstallReferrerClient? = null

    suspend fun fetchInstallReferrer(context: Context): String? = coroutineScope {
        suspendCancellableCoroutine { continuation ->
            try {
                val client = InstallReferrerClient.newBuilder(context).build()
                currentClient = client
                client.startConnection(object : InstallReferrerStateListener {
                    override fun onInstallReferrerSetupFinished(responseCode: Int) {
                        try {
                            when (responseCode) {
                                InstallReferrerClient.InstallReferrerResponse.OK -> {
                                    val referrer = client.installReferrer.installReferrer
                                    DataPreferences.getInstance(context).putString(PeekExample.KEY_REF2_DATA, client.installReferrer.referrerClickTimestampSeconds.toString())
                                    DataPreferences.getInstance(context).putString(PeekExample.KEY_REF3_DATA, client.installReferrer.referrerClickTimestampServerSeconds.toString())
                                    continuation.resume(referrer.takeIf { it.isNotEmpty() })
                                }

                                else -> continuation.resume(null)
                            }
                        } catch (e: Exception) {
                            Log.e("Referrer", "Error processing referrer data", e)
                            continuation.resume(null)
                        } finally {
                            safeEndConnection()
                        }
                    }

                    override fun onInstallReferrerServiceDisconnected() {
                        safeEndConnection()
                        if (!continuation.isCompleted) {
                            continuation.resume(null)
                        }
                    }
                })
            } catch (e: Exception) {
                safeEndConnection()
                continuation.resume(null)
            }
        }
    }

    private fun safeEndConnection() {
        runCatching { currentClient?.endConnection() }
            .onFailure { Log.w("Referrer", "End connection failed: ${it.message}") }
        currentClient = null
    }
}



