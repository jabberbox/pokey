package com.thelightphone.sample

enum class WeightUnit(val label: String) {
    LBS("lbs"),
    KG("kg"),
}

private const val LBS_PER_KG = 2.2046226218

/** Converts a value stored in lbs (the app's canonical storage unit) to [unit] for display. */
fun Double.lbsToDisplay(unit: WeightUnit): Double = when (unit) {
    WeightUnit.LBS -> this
    WeightUnit.KG -> this / LBS_PER_KG
}

/** Converts a value entered/displayed in [unit] back to lbs for storage. */
fun Double.displayToLbs(unit: WeightUnit): Double = when (unit) {
    WeightUnit.LBS -> this
    WeightUnit.KG -> this * LBS_PER_KG
}
