package com.example.data.model

data class HashrateRentalPlan(
    val id: String,
    val name: String,
    val durationDays: Int,
    val rentalCostAps: Double,
    val rentalCostUsd: Double,
    val bonusHashrateMhs: Double,
    val bonusYieldPerSecond: Double,
    val netProfitPercent: Double,
    val badgeLabel: String,
    val description: String
) {
    val totalMinedApsOverPeriod: Double
        get() = bonusYieldPerSecond * (durationDays * 86400)

    val netProfitAps: Double
        get() = totalMinedApsOverPeriod - rentalCostAps
}

data class ActiveRental(
    val planId: String,
    val planName: String,
    val startTimestamp: Long,
    val durationMillis: Long,
    val bonusHashrateMhs: Double,
    val bonusYieldPerSecond: Double
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= (startTimestamp + durationMillis)

    val remainingMillis: Long
        get() = ((startTimestamp + durationMillis) - System.currentTimeMillis()).coerceAtLeast(0L)

    val progressFraction: Float
        get() {
            val elapsed = System.currentTimeMillis() - startTimestamp
            if (durationMillis <= 0L) return 1f
            return (elapsed.toFloat() / durationMillis.toFloat()).coerceIn(0f, 1f)
        }
}

object HashrateRentalCatalog {
    val PLANS = listOf(
        HashrateRentalPlan(
            id = "RENT_7D_NANO",
            name = "Nano Node Booster",
            durationDays = 7,
            rentalCostAps = 84544.0,
            rentalCostUsd = 0.25,
            bonusHashrateMhs = 5.0,
            bonusYieldPerSecond = 0.15377,
            netProfitPercent = 10.0,
            badgeLabel = "+10% Net Yield",
            description = "Sewa akselerator hashrate 7 hari untuk boost penambangan harian Anda dengan profit bersih terjamin."
        ),
        HashrateRentalPlan(
            id = "RENT_14D_QUANTUM",
            name = "Quantum Array Cluster",
            durationDays = 14,
            rentalCostAps = 338174.0,
            rentalCostUsd = 1.0,
            bonusHashrateMhs = 25.0,
            bonusYieldPerSecond = 0.321511,
            netProfitPercent = 15.0,
            badgeLabel = "+15% Net Yield",
            description = "Sewa daya cloud computing Quantum 14 hari dengan efisiensi tinggi dan return optimal."
        ),
        HashrateRentalPlan(
            id = "RENT_30D_SUBZERO",
            name = "Sub-Zero Dedicated Rig",
            durationDays = 30,
            rentalCostAps = 1690872.0,
            rentalCostUsd = 5.0,
            bonusHashrateMhs = 100.0,
            bonusYieldPerSecond = 0.782811,
            netProfitPercent = 20.0,
            badgeLabel = "+20% Net Yield",
            description = "Sewa rig pendingin kriogenik 30 hari penuh. Output konsisten dan prioritas blok reward tinggi."
        ),
        HashrateRentalPlan(
            id = "RENT_30D_TITAN",
            name = "Titan Enterprise Grid",
            durationDays = 30,
            rentalCostAps = 6763488.0,
            rentalCostUsd = 20.0,
            bonusHashrateMhs = 450.0,
            bonusYieldPerSecond = 3.261713,
            netProfitPercent = 25.0,
            badgeLabel = "+25% Net Yield",
            description = "Kapasitas komputasi enterprise tingkat industri untuk hasil penambangan APS terbesar."
        )
    )

    fun getPlan(id: String): HashrateRentalPlan? = PLANS.find { it.id == id }
}
