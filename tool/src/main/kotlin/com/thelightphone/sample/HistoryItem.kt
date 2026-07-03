package com.thelightphone.sample

sealed class HistoryItem {
    abstract val id: Long
    abstract val timestampMillis: Long

    data class ShotItem(
        override val id: Long,
        override val timestampMillis: Long,
        val site: InjectionSite,
    ) : HistoryItem()

    data class WeightItem(
        override val id: Long,
        override val timestampMillis: Long,
        val weightLbs: Double,
    ) : HistoryItem()
}
