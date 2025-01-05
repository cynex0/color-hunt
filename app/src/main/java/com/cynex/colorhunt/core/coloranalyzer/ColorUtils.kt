package com.cynex.colorhunt.core.coloranalyzer

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils
import kotlin.math.pow
import kotlin.math.sqrt

fun Color.toRgb255(): Triple<Int, Int, Int> {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return Triple(red, green, blue)
}

fun calculateColorDelta(a: Color, b: Color): Double {
    val color1 = DoubleArray(3)
    val a = a.toRgb255()
    ColorUtils.RGBToLAB(a.first, a.second, a.third, color1)
    val b = b.toRgb255()
    val color2 = DoubleArray(3)
    ColorUtils.RGBToLAB(b.first, b.second, b.third, color2)

    val deltaL = color1[0] - color2[0]
    val deltaA = color1[1] - color2[1]
    val deltaB = color1[2] - color2[2]
    return sqrt(deltaL * deltaL + deltaA * deltaA + deltaB * deltaB)
}

fun calculateEuclideanDistance(a: Pair<Int, Int>, b: Pair<Int, Int>): Double {
    return sqrt((a.first - b.first).toDouble().pow(2) + (a.second - b.second).toDouble().pow(2))
}
