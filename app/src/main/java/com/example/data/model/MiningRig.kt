package com.example.data.model

data class MiningRig(
    val level: Int,
    val name: String,
    val modelCode: String,
    val yieldPerSecond: Double,
    val hashrateMhs: Double,
    val upgradeCostAps: Double,
    val powerWatts: Int,
    val baseTempC: Int,
    val description: String,
    val monthlyYieldPercent: Double = 0.0,
    val imageUrl: String = ""
) {
    val monthlyYieldAps: Double
        get() = yieldPerSecond * 30 * 86400

    val dailyYieldAps: Double
        get() = yieldPerSecond * 86400
}

object MiningRigsCatalog {
    val RIGS: List<MiningRig> = listOf(
        MiningRig(
            level = 1,
            name = "APS Micro Node Alpha",
            modelCode = "APS-MN-01",
            yieldPerSecond = 0.001,
            hashrateMhs = 1.25,
            upgradeCostAps = 0.0,
            powerWatts = 45,
            baseTempC = 42,
            description = "Gratis starter node untuk memulai penambangan APS Chain di Solana Mainnet. Didukung oleh RWA Treasury Buyback Pool.",
            monthlyYieldPercent = 0.0,
            imageUrl = "https://github.com/Irfan45372/Aset/blob/main/APS%20Micro%20Node.png?raw=true"
        ),
        MiningRig(
            level = 2,
            name = "APS Turbo Rig Beta",
            modelCode = "APS-TR-02",
            yieldPerSecond = 0.0007828,
            hashrateMhs = 6.8,
            upgradeCostAps = 338174.0,
            powerWatts = 120,
            baseTempC = 49,
            description = "Mesin Tier 1 (~$1 USD). Menghasilkan 0.6%/bulan yang dijamin dari 7% APY Real Business Treasury.",
            monthlyYieldPercent = 0.6
        ),
        MiningRig(
            level = 3,
            name = "APS Quantum Array",
            modelCode = "APS-QA-03",
            yieldPerSecond = 0.007828,
            hashrateMhs = 34.5,
            upgradeCostAps = 3381744.0,
            powerWatts = 280,
            baseTempC = 56,
            description = "Mesin Tier 2 (~$10 USD). Menghasilkan 0.6%/bulan dengan proteksi buyback likuiditas.",
            monthlyYieldPercent = 0.6
        ),
        MiningRig(
            level = 4,
            name = "APS Sub-Zero Cluster",
            modelCode = "APS-SZ-04",
            yieldPerSecond = 0.03914,
            hashrateMhs = 142.0,
            upgradeCostAps = 16908720.0,
            powerWatts = 550,
            baseTempC = 62,
            description = "Mesin Tier 3 (~$50 USD). Cryo-cooled cluster dengan efisiensi tinggi dan cadangan buyback 0.1%/bln.",
            monthlyYieldPercent = 0.6
        ),
        MiningRig(
            level = 5,
            name = "APS Supernova Core",
            modelCode = "APS-SC-05",
            yieldPerSecond = 0.1957,
            hashrateMhs = 620.0,
            upgradeCostAps = 84543600.0,
            powerWatts = 1100,
            baseTempC = 68,
            description = "Mesin Flagship (~$250 USD). Output performa maksimal dengan back-up dana bisnis nyata 7% APY.",
            monthlyYieldPercent = 0.6
        )
    )

    fun getRigForLevel(level: Int): MiningRig {
        return RIGS.find { it.level == level } ?: RIGS.first()
    }
}
