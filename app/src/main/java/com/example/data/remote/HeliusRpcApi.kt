package com.example.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HeliusRpcApi(private val getRpcUrl: () -> String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    suspend fun getSlotAndLatency(): Pair<Long?, Long?> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "getSlot")
                put("params", JSONArray())
            }.toString().toRequestBody(jsonMedia)

            val request = Request.Builder()
                .url(getRpcUrl())
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val latency = System.currentTimeMillis() - start
                if (!response.isSuccessful) return@withContext Pair(null, latency)
                val json = JSONObject(response.body?.string() ?: "")
                val slot = json.optLong("result", -1L)
                Pair(if (slot > 0) slot else null, latency)
            }
        } catch (_: Exception) {
            Pair(null, null)
        }
    }

    suspend fun getSolBalance(walletAddress: String): Double = withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 2)
                put("method", "getBalance")
                put("params", JSONArray().put(walletAddress))
            }.toString().toRequestBody(jsonMedia)

            val request = Request.Builder()
                .url(getRpcUrl())
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext 0.0
                val json = JSONObject(response.body?.string() ?: "")
                val result = json.optJSONObject("result")
                val lamports = result?.optLong("value", 0L) ?: 0L
                lamports.toDouble() / 1_000_000_000.0
            }
        } catch (_: Exception) {
            0.0
        }
    }

    suspend fun getTokenBalance(walletAddress: String, mintAddress: String): Double = withContext(Dispatchers.IO) {
        try {
            val config = JSONObject().apply {
                put("mint", mintAddress)
            }
            val encoding = JSONObject().apply {
                put("encoding", "jsonParsed")
            }
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 3)
                put("method", "getTokenAccountsByOwner")
                put("params", JSONArray().put(walletAddress).put(config).put(encoding))
            }.toString().toRequestBody(jsonMedia)

            val request = Request.Builder()
                .url(getRpcUrl())
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext 0.0
                val json = JSONObject(response.body?.string() ?: "")
                val result = json.optJSONObject("result")
                val value = result?.optJSONArray("value")
                if (value != null && value.length() > 0) {
                    var total = 0.0
                    for (i in 0 until value.length()) {
                        val acc = value.getJSONObject(i)
                        val accountData = acc.optJSONObject("account")?.optJSONObject("data")
                        val parsed = accountData?.optJSONObject("parsed")
                        val info = parsed?.optJSONObject("info")
                        val tokenAmount = info?.optJSONObject("tokenAmount")
                        val uiAmount = tokenAmount?.optDouble("uiAmount", 0.0) ?: 0.0
                        total += uiAmount
                    }
                    total
                } else {
                    0.0
                }
            }
        } catch (_: Exception) {
            0.0
        }
    }
}
