package com.example

import com.example.data.model.RpcConfig
import com.example.data.remote.ApsPriceOracleService
import com.example.data.remote.ApsPriceQuote
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ApsPriceOracleServiceTest {

    private val oracleService = ApsPriceOracleService()

    @Test
    fun calculateApsForFee_defaultParity_tenDollarsEqualsTargetAPS() {
        // At default parity (1 USD = 344,850.3 APS), 10 USD should equal exactly 3,448,503 APS
        val requiredAps = oracleService.calculateApsForFee(
            feeUsd = 10.0,
            apsPerUsd = 344850.3
        )
        assertEquals(3448503.0, requiredAps, 0.01)
    }

    @Test
    fun calculateApsForFee_whenApsPriceIsTwoDollars_tenDollarsEqualsFiveAPS() {
        // If 1 APS = $2.00 USD, $10 USD fee requires 10 / 2 = 5 APS
        val requiredAps = oracleService.calculateApsForFeeWithTokenPrice(
            feeUsd = 10.0,
            priceUsdPerToken = 2.0
        )
        assertEquals(5.0, requiredAps, 0.0001)
    }

    @Test
    fun calculateApsForFee_whenApsPriceIsFiveDollars_tenDollarsEqualsTwoAPS() {
        // If 1 APS = $5.00 USD, $10 USD fee requires 10 / 5 = 2 APS
        val requiredAps = oracleService.calculateApsForFeeWithTokenPrice(
            feeUsd = 10.0,
            priceUsdPerToken = 5.0
        )
        assertEquals(2.0, requiredAps, 0.0001)
    }

    @Test
    fun priceQuote_dynamicUsdConversion_isAccurate() {
        val quote = ApsPriceQuote(
            priceUsdPerToken = 0.000002899809,
            apsPerUsd = 344850.3,
            dexSource = "Jupiter v2 DEX",
            isLive = true
        )

        // $10 fee in APS
        val feeAps = quote.calculateApsForFeeUsd(10.0)
        assertEquals(3448503.0, feeAps, 1.0)

        // 3,448,503 APS converted back to USD
        val usdVal = quote.calculateUsdForAps(3448503.0)
        assertEquals(10.0, usdVal, 0.01)
    }

    @Test
    fun fetchCurrentPriceQuote_returnsValidQuoteOrFallback() = runBlocking {
        val quote = oracleService.fetchCurrentPriceQuote(RpcConfig.APS_MINT_ADDRESS)
        assertNotNull(quote)
        assertTrue("Price per token must be positive", quote.priceUsdPerToken > 0.0)
        assertTrue("APS per USD must be positive", quote.apsPerUsd > 0.0)
        assertNotNull(quote.dexSource)

        val feeForTenUsd = quote.calculateApsForFeeUsd(10.0)
        assertTrue("Fee for 10 USD must be positive", feeForTenUsd > 0.0)
    }
}
