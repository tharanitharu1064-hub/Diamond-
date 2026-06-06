package com.example.diamond

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.sqrt

data class ColorLegendItem(
    val index: Int,
    val label: String,
    val colorValue: Int, // ARGB format
    val count: Int,
    val dmcEstimate: String // DMC code approximation
) {
    val hexString: String
        get() = String.format("#%06X", 0xFFFFFF and colorValue)
}

data class PixelCell(
    val x: Int,
    val y: Int,
    val origColorValue: Int,
    val quantizedColorValue: Int,
    val label: String,
    val colorIndex: Int
)

data class DiamondArtKit(
    val cells: List<PixelCell>,
    val gridWidth: Int,
    val gridHeight: Int,
    val legend: List<ColorLegendItem>,
    val totalDiamonds: Int
)

object PixelQuantizer {

    // Unique marking alphabets and numbers for mapping colors
    private val ALPHABETS = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", 
        "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "0", 
        "H1", "H2", "H3", "H4", "H5"
    )

    /**
     * Map arbitrary RGB colors to nearest DMC Embroidery Floss colors
     */
    private val DMC_COLORS = listOf(
        Pair("310", Color.rgb(0, 0, 0)),         // Black
        Pair("B5200", Color.rgb(255, 255, 255)), // Snow White
        Pair("white", Color.rgb(240, 240, 240)),  // Off White
        Pair("415", Color.rgb(211, 215, 221)),   // Chrome Grey
        Pair("318", Color.rgb(178, 185, 196)),   // Granite Grey
        Pair("414", Color.rgb(141, 147, 158)),   // Dark Steel Grey
        Pair("317", Color.rgb(112, 117, 126)),   // Charcoal Grey
        Pair("666", Color.rgb(224, 0, 36)),      // Bright Red
        Pair("321", Color.rgb(193, 10, 42)),     // Red
        Pair("498", Color.rgb(161, 8, 35)),      // Dark Red
        Pair("814", Color.rgb(120, 4, 25)),      // Deep Wine
        Pair("995", Color.rgb(0, 114, 188)),     // Electric Blue
        Pair("796", Color.rgb(16, 44, 116)),     // Royal Blue
        Pair("820", Color.rgb(10, 24, 74)),      // Very Dark Navy
        Pair("906", Color.rgb(58, 163, 44)),     // Apple Green
        Pair("699", Color.rgb(0, 101, 24)),      // Green
        Pair("907", Color.rgb(143, 212, 0)),     // Light Olive Green
        Pair("743", Color.rgb(253, 203, 48)),    // Medium Yellow
        Pair("972", Color.rgb(255, 181, 0)),     // Deep Yellow Gold
        Pair("740", Color.rgb(255, 121, 0)),     // Tangerine Orange
        Pair("606", Color.rgb(255, 36, 0)),      // Bright Orange-Red
        Pair("602", Color.rgb(226, 41, 117)),    // Cranberry Pink
        Pair("210", Color.rgb(194, 159, 211)),   // Lavender
        Pair("550", Color.rgb(92, 11, 115)),     // Ultra Dark Violet
        Pair("433", Color.rgb(122, 60, 15)),     // Chocolate Brown
        Pair("898", Color.rgb(72, 35, 8)),       // Dark Sable Brown
        Pair("938", Color.rgb(42, 15, 2)),       // Ultra Dark Coffee
        Pair("155", Color.rgb(151, 151, 219)),   // Light Violet Blue
        Pair("959", Color.rgb(0, 201, 167)),     // Medium Seagreen
        Pair("964", Color.rgb(167, 245, 229))    // Light Sea Green
    )

    private fun findNearestDmc(r: Int, g: Int, b: Int): String {
        var closestCode = "310"
        var minDistance = Double.MAX_VALUE
        for (dmc in DMC_COLORS) {
            val dmcColor = dmc.second
            val dr = r - Color.red(dmcColor)
            val dg = g - Color.green(dmcColor)
            val db = b - Color.blue(dmcColor)
            val dist = (dr * dr + dg * dg + db * db).toDouble()
            if (dist < minDistance) {
                minDistance = dist
                closestCode = dmc.first
            }
        }
        return closestCode
    }

    /**
     * Resizes a bitmap so its total area (width * height) is close to but <= targetCellLimit.
     * Keeps the original aspect ratio.
     */
    fun getTargetDimensions(origWidth: Int, origHeight: Int, targetCellLimit: Int): Pair<Int, Int> {
        val aspect = origWidth.toFloat() / origHeight.toFloat()
        // W * H = targetCellLimit
        // W * (W / aspect) = targetCellLimit -> W^2 = targetCellLimit * aspect
        val w = sqrt(targetCellLimit.toDouble() * aspect).toInt()
        val h = (w / aspect).toInt()

        // Clamp values to sane ranges (at least 5 pixels, up to safe limits)
        val clampedW = w.coerceIn(5, 320)
        val clampedH = h.coerceIn(5, 320)

        // Adjust if they multiply to exceed target slightly
        var finalW = clampedW
        var finalH = clampedH
        if (finalW * finalH > targetCellLimit) {
            // scale down slightly
            val scale = sqrt(targetCellLimit.toFloat() / (finalW * finalH))
            finalW = (finalW * scale).toInt().coerceAtLeast(5)
            finalH = (finalH * scale).toInt().coerceAtLeast(5)
        }
        return Pair(finalW, finalH)
    }

    /**
     * Converts a source bitmap into a pixelated DiamondArtKit using K-Means Clustering on a grid.
     */
    fun createDiamondKit(
        source: Bitmap,
        targetCellLimit: Int,
        paletteSize: Int,
        grayscaleOnly: Boolean
    ): DiamondArtKit {
        // 1. Get resize bounds
        val (gridW, gridH) = getTargetDimensions(source.width, source.height, targetCellLimit)

        // 2. Scale bitmap to grid dimensions
        val scaledBitmap = Bitmap.createScaledBitmap(source, gridW, gridH, true)

        // 3. Extract pixels & apply grayscale transformation if requested
        val pixelCount = gridW * gridH
        val pixels = IntArray(pixelCount)
        scaledBitmap.getPixels(pixels, 0, gridW, 0, 0, gridW, gridH)

        val workingPixels = IntArray(pixelCount)
        for (i in 0 until pixelCount) {
            val color = pixels[i]
            if (grayscaleOnly) {
                val r = Color.red(color)
                val g = Color.green(color)
                val b = Color.blue(color)
                // Standard grayscale luminance formula
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt().coerceIn(0, 255)
                workingPixels[i] = Color.rgb(gray, gray, gray)
            } else {
                workingPixels[i] = color
            }
        }

        // 4. K-Means clustering algorithm initialization
        // Pick top distinct colors or sliced distribution as centers for stability
        val centers = initClusterCenters(workingPixels, paletteSize)

        // Run standard K-means iterations (5 times is fast and highly effective)
        val clusterAssignments = IntArray(pixelCount)
        val iterCount = 6

        for (iter in 0 until iterCount) {
            // Step A: Assignment
            for (p in 0 until pixelCount) {
                val pxColor = workingPixels[p]
                val pr = Color.red(pxColor)
                val pg = Color.green(pxColor)
                val pb = Color.blue(pxColor)

                var bestCenterIdx = 0
                var minDistanceSq = Double.MAX_VALUE

                for (c in centers.indices) {
                    val cr = Color.red(centers[c])
                    val cg = Color.green(centers[c])
                    val cb = Color.blue(centers[c])

                    val dr = pr - cr
                    val dg = pg - cg
                    val db = pb - cb
                    val distSq = (dr * dr + dg * dg + db * db).toDouble()

                    if (distSq < minDistanceSq) {
                        minDistanceSq = distSq
                        bestCenterIdx = c
                    }
                }
                clusterAssignments[p] = bestCenterIdx
            }

            // Step B: Recompute Centers
            val sumsRed = LongArray(paletteSize)
            val sumsGreen = LongArray(paletteSize)
            val sumsBlue = LongArray(paletteSize)
            val counts = IntArray(paletteSize)

            for (p in 0 until pixelCount) {
                val owner = clusterAssignments[p]
                val pxColor = workingPixels[p]
                sumsRed[owner] += Color.red(pxColor).toLong()
                sumsGreen[owner] += Color.green(pxColor).toLong()
                sumsBlue[owner] += Color.blue(pxColor).toLong()
                counts[owner]++
            }

            for (c in 0 until paletteSize) {
                if (counts[c] > 0) {
                    val avgR = (sumsRed[c] / counts[c]).toInt()
                    val avgG = (sumsGreen[c] / counts[c]).toInt()
                    val avgB = (sumsBlue[c] / counts[c]).toInt()
                    centers[c] = Color.rgb(avgR, avgG, avgB)
                } else {
                    // Re-seed randomly from input array if empty to retain variance
                    centers[c] = workingPixels[(workingPixels.size * Math.random()).toInt()]
                }
            }
        }

        // 5. Gather frequency statistics and map centers to visual sorting
        // Let's count how many pixels are in each cluster owner
        val counts = IntArray(paletteSize)
        for (owner in clusterAssignments) {
            counts[owner]++
        }

        // Pair center colors with counts, sorted by frequency descending
        val sortedIndices = counts.indices
            .filter { counts[it] > 0 }
            .sortedByDescending { counts[it] }

        // Create the Legend Mapping
        val legendMap = mutableMapOf<Int, ColorLegendItem>() // Key: original center index
        val cellsList = java.util.ArrayList<PixelCell>(pixelCount)

        sortedIndices.forEachIndexed { finalSortedIdx, originalIdx ->
            val color = centers[originalIdx]
            val alphabetTag = if (finalSortedIdx < ALPHABETS.size) {
                ALPHABETS[finalSortedIdx]
            } else {
                "Z${finalSortedIdx - ALPHABETS.size + 1}"
            }

            // DMC Estimation
            val dmc = findNearestDmc(Color.red(color), Color.green(color), Color.blue(color))

            legendMap[originalIdx] = ColorLegendItem(
                index = finalSortedIdx,
                label = alphabetTag,
                colorValue = color,
                count = counts[originalIdx],
                dmcEstimate = dmc
            )
        }

        // 6. Build the PixelCell grid items
        for (y in 0 until gridH) {
            for (x in 0 until gridW) {
                val indexInArray = y * gridW + x
                val origColor = pixels[indexInArray]
                val origClusterIdx = clusterAssignments[indexInArray]

                val mappedItem = legendMap[origClusterIdx] ?: ColorLegendItem(
                    index = 99,
                    label = "?",
                    colorValue = Color.GRAY,
                    count = 0,
                    dmcEstimate = "310"
                )

                cellsList.add(
                    PixelCell(
                        x = x,
                        y = y,
                        origColorValue = origColor,
                        quantizedColorValue = mappedItem.colorValue,
                        label = mappedItem.label,
                        colorIndex = mappedItem.index
                    )
                )
            }
        }

        val sortedLegends = legendMap.values.sortedBy { it.index }

        return DiamondArtKit(
            cells = cellsList,
            gridWidth = gridW,
            gridHeight = gridH,
            legend = sortedLegends,
            totalDiamonds = pixelCount
        )
    }

    /**
     * Carefully presets K starting cluster candidates to ensure representative colors
     * are chosen, avoiding getting trapped in trivial local minima.
     */
    private fun initClusterCenters(pixels: IntArray, paletteSize: Int): IntArray {
        val result = IntArray(paletteSize)
        if (pixels.isEmpty()) return result

        // Compile some initial candidates:
        // Try slicing the array evenly, ensuring diverse sampling, then sort by presence
        val step = (pixels.size / paletteSize).coerceAtLeast(1)
        var idx = 0
        for (i in 0 until paletteSize) {
            val selIdx = (i * step).coerceAtMost(pixels.size - 1)
            result[i] = pixels[selIdx]
        }
        return result
    }
}
