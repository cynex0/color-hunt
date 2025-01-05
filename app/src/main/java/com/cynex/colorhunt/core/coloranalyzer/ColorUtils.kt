package com.cynex.colorhunt.core.coloranalyzer

import androidx.core.graphics.ColorUtils
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.pow
import kotlin.math.sqrt

fun calculateColorDelta(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>): Double {
    val color1 = DoubleArray(3)
    ColorUtils.RGBToLAB(a.first, a.second, a.third, color1)
    val color2 = DoubleArray(3)
    ColorUtils.RGBToLAB(b.first, b.second, b.third, color2)

    val deltaL = color1[0] - color2[0]
    val deltaA = color1[1] - color2[1]
    val deltaB = color1[2] - color2[2]
    return sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB)
}

fun calculateColorDelta(a: String, b: String): Double {
    val color1 = android.graphics.Color.parseColor(a)
    val color2 = android.graphics.Color.parseColor(b)
    return calculateColorDelta(Triple(color1.red, color1.green, color1.blue), Triple(color2.red, color2.green, color2.blue))
}

fun calculateEuclideanDistance(a: Pair<Int, Int>, b: Pair<Int, Int>): Double {
    return sqrt((a.first - b.first).toDouble().pow(2) + (a.second - b.second).toDouble().pow(2))
}
