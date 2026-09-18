package com.example.data.remote

import com.example.data.model.RpcConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ApsPriceOracleService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun fetchApsPrice(): Double = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${RpcConfig.DEFAULT_CLOUDFLARE_RELAYER_URL}/api/price/aps")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val price = json.optDouble("apsPerUsd", RpcConfig.DEFAULT_APS_PER_USD)
                    if (price > 0) price else RpcConfig.DEFAULT_APS_PER_USD
                } else {
                    RpcConfig.DEFAULT_APS_PER_USD
                }
            }
        } catch (_: Exception) {
            RpcConfig.DEFAULT_APS_PER_USD
        }
    }
}
