package com.example.data.model

data class UssdCode(
    val title: String,
    val code: String,
    val description: String,
    val iconType: UssdIconType,
    val isPrimary: Boolean = false
)

enum class UssdIconType {
    AIRTIME,
    DATA_BUY,
    DATA_BALANCE,
    BORROW,
    VOUCHER,
    TRANSFER
}

object UssdDirectory {
    val PRIMARY_CODES = listOf(
        UssdCode(
            title = "Check Airtime",
            code = "*310#",
            description = "Check main balance & voice bonuses",
            iconType = UssdIconType.AIRTIME,
            isPrimary = true
        ),
        UssdCode(
            title = "Buy Data / Plans",
            code = "*312#",
            description = "Open menu to buy data or other plans",
            iconType = UssdIconType.DATA_BUY,
            isPrimary = true
        ),
        UssdCode(
            title = "Check Data Balance",
            code = "*323#",
            description = "Check active data balance & expiry",
            iconType = UssdIconType.DATA_BALANCE,
            isPrimary = true
        )
    )

    val ALL_CODES = listOf(
        UssdCode(
            title = "Check Airtime Balance",
            code = "*310#",
            description = "Check main airtime balance and voice bonuses across all networks",
            iconType = UssdIconType.AIRTIME,
            isPrimary = true
        ),
        UssdCode(
            title = "Buy Data & Other Plans",
            code = "*312#",
            description = "Open the menu to buy data bundles, social plans, or tariff plans",
            iconType = UssdIconType.DATA_BUY,
            isPrimary = true
        ),
        UssdCode(
            title = "Check Data Balance",
            code = "*323#",
            description = "Check active data balance, bonus volume & plan validity",
            iconType = UssdIconType.DATA_BALANCE,
            isPrimary = true
        ),
        UssdCode(
            title = "Borrow Airtime / Data",
            code = "*303#",
            description = "Borrow airtime or data bundles in an emergency",
            iconType = UssdIconType.BORROW,
            isPrimary = false
        ),
        UssdCode(
            title = "Recharge Voucher",
            code = "*311#",
            description = "Load airtime scratch card PIN or physical voucher",
            iconType = UssdIconType.VOUCHER,
            isPrimary = false
        ),
        UssdCode(
            title = "Share / Transfer",
            code = "*321#",
            description = "Transfer airtime or gift data to another line",
            iconType = UssdIconType.TRANSFER,
            isPrimary = false
        )
    )
}
