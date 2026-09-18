package com.example.data.remote

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.RpcConfig
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SolanaWalletAdapterService(private val context: Context) {
    private val walletAdapter = MobileWalletAdapter(
        connectionIdentity = ConnectionIdentity(
            identityUri = Uri.parse(RpcConfig.DEFAULT_CLOUDFLARE_RELAYER_URL),
            iconUri = Uri.parse("${RpcConfig.DEFAULT_CLOUDFLARE_RELAYER_URL}/favicon.ico"),
            identityName = "APS Miner"
        )
    )

    suspend fun connect(sender: ActivityResultSender): String? = withContext(Dispatchers.IO) {
        try {
            val result = walletAdapter.transact(sender) {
                val authResult = authorize(
                    identityUri = Uri.parse(RpcConfig.DEFAULT_CLOUDFLARE_RELAYER_URL),
                    iconUri = Uri.parse("${RpcConfig.DEFAULT_CLOUDFLARE_RELAYER_URL}/favicon.ico"),
                    identityName = "APS Miner",
                    rpcCluster = RpcCluster.MainnetBeta
                )
                authResult.publicKey
            }
            when (result) {
                is TransactionResult.Success -> {
                    result.payload?.let { pubKeyBytes ->
                        Base58.encode(pubKeyBytes)
                    }
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun openPhantomPlayStore(activity: Activity) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=app.phantom"))
            activity.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=app.phantom"))
            activity.startActivity(webIntent)
        }
    }
}

object Base58 {
    private const val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
    private val INDEXES = IntArray(128) { -1 }.apply {
        for (i in ALPHABET.indices) {
            this[ALPHABET[i].code] = i
        }
    }

    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""
        var zeros = 0
        while (zeros < input.size && input[zeros] == 0.toByte()) {
            zeros++
        }
        val encoded = CharArray(input.size * 2)
        var outputStart = encoded.size
        var inputStart = zeros
        while (inputStart < input.size) {
            val decodedByte = input[inputStart].toInt() and 0xFF
            var carry = decodedByte
            var j = encoded.size - 1
            while (j >= outputStart || carry != 0) {
                val temp = ((encoded[j].code.takeIf { it != 0 }?.let { INDEXES[it] } ?: 0) shl 8) + carry
                encoded[j] = ALPHABET[temp % 58]
                carry = temp / 58
                j--
            }
            outputStart = j + 1
            inputStart++
        }
        while (outputStart < encoded.size && encoded[outputStart] == '1') {
            outputStart++
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = '1'
        }
        return String(encoded, outputStart, encoded.size - outputStart)
    }

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)
        val input58 = ByteArray(input.length)
        for (i in input.indices) {
            val c = input[i]
            val digit = if (c.code < 128) INDEXES[c.code] else -1
            if (digit < 0) return ByteArray(0)
            input58[i] = digit.toByte()
        }
        var zeros = 0
        while (zeros < input58.size && input58[zeros] == 0.toByte()) {
            zeros++
        }
        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        var inputStart = zeros
        while (inputStart < input58.size) {
            var carry = input58[inputStart].toInt() and 0xFF
            var j = decoded.size - 1
            while (j >= outputStart || carry != 0) {
                val temp = (decoded[j].toInt() and 0xFF) * 58 + carry
                decoded[j] = (temp and 0xFF).toByte()
                carry = temp ushr 8
                j--
            }
            outputStart = j + 1
            inputStart++
        }
        while (outputStart < decoded.size && decoded[outputStart] == 0.toByte()) {
            outputStart++
        }
        val result = ByteArray(zeros + (decoded.size - outputStart))
        System.arraycopy(decoded, outputStart, result, zeros, decoded.size - outputStart)
        return result
    }
}
