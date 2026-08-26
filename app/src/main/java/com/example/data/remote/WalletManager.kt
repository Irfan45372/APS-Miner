package com.example.data.remote

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.data.local.PreferencesManager
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class WalletSessionState {
    object Disconnected : WalletSessionState()
    data class Connecting(val message: String = "Connecting to Phantom Wallet...") : WalletSessionState()
    data class Connected(
        val publicKey: String,
        val accountLabel: String? = null,
        val authToken: String? = null,
        val walletName: String = "Phantom"
    ) : WalletSessionState()
    data class Error(val message: String, val throwable: Throwable? = null) : WalletSessionState()
}

/**
 * Base58 utility for encoding/decoding Solana Public Keys
 */
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
        while (zeros < input.size && input[zeros].toInt() == 0) {
            zeros++
        }
        val encoded = CharArray(input.size * 2)
        var outputStart = encoded.size
        var inputStart = zeros
        while (inputStart < input.size) {
            var carry = input[inputStart].toInt() and 0xFF
            var j = encoded.size - 1
            while (j >= outputStart || carry != 0) {
                carry += (256 * (if (j < encoded.size) INDEXES.getOrElse(encoded[j].code) { 0 } else 0))
                // base 58 division
                encoded[j] = ALPHABET[carry % 58]
                carry /= 58
                j--
            }
            outputStart = j + 1
            inputStart++
        }
        while (outputStart < encoded.size && encoded[outputStart] == ALPHABET[0]) {
            outputStart++
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ALPHABET[0]
        }
        return String(encoded, outputStart, encoded.size - outputStart)
    }

    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)
        val input58 = ByteArray(input.length)
        for (i in input.indices) {
            val c = input[i]
            val digit = if (c.code < 128) INDEXES[c.code] else -1
            if (digit < 0) throw IllegalArgumentException("Illegal character $c in Base58 string")
            input58[i] = digit.toByte()
        }
        var zeros = 0
        while (zeros < input58.size && input58[zeros].toInt() == 0) {
            zeros++
        }
        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        var inputStart = zeros
        while (inputStart < input58.size) {
            var carry = input58[inputStart].toInt() and 0xFF
            var j = decoded.size - 1
            while (j >= outputStart || carry != 0) {
                carry += 58 * (decoded[j].toInt() and 0xFF)
                decoded[j] = (carry % 256).toByte()
                carry /= 256
                j--
            }
            outputStart = j + 1
            inputStart++
        }
        while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) {
            outputStart++
        }
        val result = ByteArray(zeros + (decoded.size - outputStart))
        System.arraycopy(decoded, outputStart, result, zeros, decoded.size - outputStart)
        return result
    }
}

/**
 * WalletManager handles Solana Phantom Wallet integration using Solana Mobile Wallet Adapter (MWA).
 * Manages authorization, session states, tokens, and reauthorization.
 */
class WalletManager(
    private val context: Context,
    private val prefs: PreferencesManager,
    private val scope: CoroutineScope
) {
    private val _sessionState = MutableStateFlow<WalletSessionState>(WalletSessionState.Disconnected)
    val sessionState: StateFlow<WalletSessionState> = _sessionState.asStateFlow()

    private val connectionIdentity = ConnectionIdentity(
        identityUri = Uri.parse("https://aps-mining-rig.solana.io"),
        iconUri = Uri.parse("https://raw.githubusercontent.com/solana-labs/token-list/main/assets/mainnet/5Jbt6zLztKuRF9hc7C8y2VemxMguP86ibDoJzetkpump/logo.png"),
        identityName = "APS Solana Mining Node"
    )

    private val adapter = MobileWalletAdapter(connectionIdentity)

    private var authToken: String? = null

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val savedAddress = prefs.walletAddress
        val savedToken = prefs.walletAuthToken
        if (savedAddress.isNotBlank()) {
            authToken = savedToken
            _sessionState.value = WalletSessionState.Connected(
                publicKey = savedAddress,
                accountLabel = "Stored Session",
                authToken = savedToken,
                walletName = "Phantom Wallet"
            )
        }
    }

    /**
     * Connect to Phantom / Solana Mobile Wallet via MWA intent
     */
    fun connect(
        sender: ActivityResultSender,
        onSuccess: ((String) -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        scope.launch {
            _sessionState.update { WalletSessionState.Connecting("Connecting to Phantom...") }
            withContext(Dispatchers.IO) {
                try {
                    val result = adapter.connect(sender)

                    when (result) {
                        is TransactionResult.Success -> {
                            val payload = result.payload
                            val (base58PubKey, token) = extractAddressAndToken(payload)

                            if (base58PubKey.isNotBlank()) {
                                authToken = token
                                prefs.walletAddress = base58PubKey
                                prefs.walletAuthToken = token

                                val connectedState = WalletSessionState.Connected(
                                    publicKey = base58PubKey,
                                    accountLabel = "Phantom Account",
                                    authToken = token,
                                    walletName = "Phantom"
                                )
                                _sessionState.value = connectedState

                                withContext(Dispatchers.Main) {
                                    onSuccess?.invoke(base58PubKey)
                                }
                            } else {
                                val errMsg = "No public key returned by Phantom wallet."
                                _sessionState.value = WalletSessionState.Error(errMsg)
                                withContext(Dispatchers.Main) {
                                    onError?.invoke(errMsg)
                                }
                            }
                        }
                        is TransactionResult.Failure -> {
                            val errMsg = result.e.message ?: "Connection rejected by Phantom Wallet"
                            _sessionState.value = WalletSessionState.Error(errMsg, result.e)
                            withContext(Dispatchers.Main) {
                                onError?.invoke(errMsg)
                            }
                        }
                        is TransactionResult.NoWalletFound -> {
                            val errMsg = "No Solana MWA Wallet (e.g. Phantom) found on this device."
                            _sessionState.value = WalletSessionState.Error(errMsg)
                            withContext(Dispatchers.Main) {
                                onError?.invoke(errMsg)
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errMsg = e.localizedMessage ?: "Failed to connect via Mobile Wallet Adapter"
                    _sessionState.value = WalletSessionState.Error(errMsg, e)
                    withContext(Dispatchers.Main) {
                        onError?.invoke(errMsg)
                    }
                }
            }
        }
    }

    /**
     * Disconnect and clear session
     */
    fun disconnect(
        sender: ActivityResultSender? = null,
        onComplete: (() -> Unit)? = null
    ) {
        if (sender != null) {
            scope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        adapter.disconnect(sender)
                    } catch (_: Exception) {
                        // ignore deauth network exceptions during local logout
                    }
                }
            }
        }

        authToken = null
        prefs.walletAddress = ""
        prefs.walletAuthToken = null
        _sessionState.value = WalletSessionState.Disconnected
        onComplete?.invoke()
    }

    /**
     * Connect manually using address string (e.g. for emulator testing or direct address input)
     */
    fun connectManualAddress(address: String): Boolean {
        val trimmed = address.trim()
        if (trimmed.length < 32) return false

        prefs.walletAddress = trimmed
        _sessionState.value = WalletSessionState.Connected(
            publicKey = trimmed,
            accountLabel = "Linked Address",
            authToken = null,
            walletName = "Phantom (Manual/Web)"
        )
        return true
    }

    fun getConnectedAddress(): String? {
        val current = _sessionState.value
        return if (current is WalletSessionState.Connected) current.publicKey else null
    }

    fun isConnected(): Boolean {
        return _sessionState.value is WalletSessionState.Connected
    }

    private fun extractAddressAndToken(payload: Any): Pair<String, String?> {
        try {
            val payloadClass = payload.javaClass

            // Check if accounts field/method exists (MWA 2.0)
            val accountsMethod = payloadClass.methods.firstOrNull { it.name == "getAccounts" || it.name == "accounts" }
            if (accountsMethod != null) {
                val accounts = accountsMethod.invoke(payload) as? List<*>
                val firstAccount = accounts?.firstOrNull()
                if (firstAccount != null) {
                    val accClass = firstAccount.javaClass
                    val pkMethod = accClass.methods.firstOrNull { it.name == "getPublicKey" || it.name == "publicKey" || it.name == "getAddress" || it.name == "address" }
                    val pk = pkMethod?.invoke(firstAccount)
                    val base58 = when (pk) {
                        is ByteArray -> Base58.encode(pk)
                        is String -> pk
                        else -> ""
                    }
                    val tokenMethod = payloadClass.methods.firstOrNull { it.name == "getAuthToken" || it.name == "authToken" }
                    val token = tokenMethod?.invoke(payload) as? String
                    if (base58.isNotBlank()) return Pair(base58, token)
                }
            }

            // Check if direct publicKey exists (MWA 1.0)
            val pkMethod = payloadClass.methods.firstOrNull { it.name == "getPublicKey" || it.name == "publicKey" || it.name == "getAddress" || it.name == "address" }
            if (pkMethod != null) {
                val pk = pkMethod.invoke(payload)
                val base58 = when (pk) {
                    is ByteArray -> Base58.encode(pk)
                    is String -> pk
                    else -> ""
                }
                val tokenMethod = payloadClass.methods.firstOrNull { it.name == "getAuthToken" || it.name == "authToken" }
                val token = tokenMethod?.invoke(payload) as? String
                if (base58.isNotBlank()) return Pair(base58, token)
            }
        } catch (_: Exception) {}
        return Pair("", null)
    }
}
