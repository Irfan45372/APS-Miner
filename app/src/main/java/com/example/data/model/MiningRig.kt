package com.example.data.model

data class MiningRig(
    val level: Int,
    val name: String,
    val modelCode: String,
    val yieldPerSecond: Double, // APS per second (calculated from 0.6%/month sustainable rate)
    val hashrateMhs: Double,    // MH/s
    val upgradeCostAps: Double,
    val powerWatts: Int,
    val baseTempC: Int,
    val description: String,
    val monthlyYieldPercent: Double = 0.60, // 0.6% per month sustainable yield
    val imageUrl: String = "https://github.com/Irfan45372/Aset/blob/main/APS%20Micro%20Node.png?raw=true"
) {
    val monthlyYieldAps: Double
        get() = upgradeCostAps * (monthlyYieldPercent / 100.0)

    val dailyYieldAps: Double
        get() = yieldPerSecond * 86400.0
}

object MiningRigsCatalog {
    // 1 month = 30 days = 2,592,000 seconds
    // Monthly rate = 0.60% (0.006)
    // Yield/sec = (Cost * 0.006) / 2,592,000
    val RIGS = listOf(
        MiningRig(
            level = 1,
            name = "APS Micro Node Alpha",
            modelCode = "APS-MN-01",
            yieldPerSecond = 0.001, // Faucet starter mode (~86.4 APS/day)
            hashrateMhs = 1.25,
            upgradeCostAps = 0.0,
            powerWatts = 45,
            baseTempC = 42,
            monthlyYieldPercent = 0.0,
            description = "Gratis starter node untuk mencoba sistem mining Solana. Didukung oleh RWA Treasury Buyback Pool."
        ),
        MiningRig(
            level = 2,
            name = "APS Turbo Rig Beta",
            modelCode = "APS-TR-02",
            // Cost: 338,174 APS (~$1 USD) -> 0.6%/bln = 2,029.04 APS/bln -> 0.0007828 APS/s
            yieldPerSecond = 0.0007828,
            hashrateMhs = 6.80,
            upgradeCostAps = 338174.0, // ~$1 USD equivalent
            powerWatts = 120,
            baseTempC = 49,
            monthlyYieldPercent = 0.60,
            description = "Mesin Tier 1 (~$1 USD). Menghasilkan 0.6%/bulan yang dijamin dari 7% APY Real Business Treasury."
        ),
        MiningRig(
            level = 3,
            name = "APS Quantum Array",
            modelCode = "APS-QA-03",
            // Cost: 3,381,744 APS (~$10 USD) -> 0.6%/bln = 20,290.46 APS/bln -> 0.007828 APS/s
            yieldPerSecond = 0.007828,
            hashrateMhs = 34.50,
            upgradeCostAps = 3381744.0, // ~$10 USD equivalent
            powerWatts = 280,
            baseTempC = 56,
            monthlyYieldPercent = 0.60,
            description = "Mesin Tier 2 (~$10 USD). Menghasilkan 0.6%/bulan dengan proteksi buyback likuiditas."
        ),
        MiningRig(
            level = 4,
            name = "APS Sub-Zero Cluster",
            modelCode = "APS-SZ-04",
            // Cost: 16,908,720 APS (~$50 USD) -> 0.6%/bln = 101,452.32 APS/bln -> 0.03914 APS/s
            yieldPerSecond = 0.03914,
            hashrateMhs = 142.00,
            upgradeCostAps = 16908720.0, // ~$50 USD equivalent
            powerWatts = 550,
            baseTempC = 62,
            monthlyYieldPercent = 0.60,
            description = "Mesin Tier 3 (~$50 USD). Cryo-cooled cluster dengan efisiensi tinggi dan cadangan buyback 0.1%/bln."
        ),
        MiningRig(
            level = 5,
            name = "APS Supernova Core",
            modelCode = "APS-SC-05",
            // Cost: 84,543,600 APS (~$250 USD) -> 0.6%/bln = 507,261.60 APS/bln -> 0.1957 APS/s
            yieldPerSecond = 0.1957,
            hashrateMhs = 620.00,
            upgradeCostAps = 84543600.0, // ~$250 USD equivalent
            powerWatts = 1100,
            baseTempC = 68,
            monthlyYieldPercent = 0.60,
            description = "Mesin Flagship (~$250 USD). Output performa maksimal dengan back-up dana bisnis nyata 7% APY."
        )
    )

    fun getRigForLevel(level: Int): MiningRig {
        return RIGS.firstOrNull { it.level == level } ?: RIGS.first()
    }
}
