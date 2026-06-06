package com.example.diamond

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun DiamondGridViewer(
    kit: DiamondArtKit,
    viewMode: ViewMode,
    highlightedColorLabel: String?,
    modifier: Modifier = Modifier,
    onCellHover: (PixelCell?) -> Unit = {}
) {
    // Zoom and Pan transformations
    var scale by remember { mutableStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Double tab or swipe resets the canvas view position
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(Color(0xFF1E1E24))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .pointerInput(kit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Zoom limits from 0.8x up to 45.0x
                    scale = (scale * zoom).coerceIn(0.8f, 45.0f)
                    
                    // Prevent crazy offsets when scale is low
                    if (scale <= 1.0f) {
                        offset = Offset.Zero
                    } else {
                        offset += pan
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasW = size.width
            val canvasH = size.height

            if (kit.gridWidth <= 0 || kit.gridHeight <= 0) return@Canvas

            // Compute default cell dimensions to fit within canvas bounds nicely
            val maxColW = canvasW / kit.gridWidth
            val maxRowH = canvasH / kit.gridHeight
            val cellSize = min(maxColW, maxRowH)

            // Center grid inside canvas
            val startX = (canvasW - (cellSize * kit.gridWidth)) / 2f
            val startY = (canvasH - (cellSize * kit.gridHeight)) / 2f

            val currentCellSize = cellSize * scale

            // Paint for text
            val textPaint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }

            drawIntoCanvas { composeCanvas ->
                val nativeCanvas = composeCanvas.nativeCanvas

                // Track global translation and scaling manually or using native graphics projection
                nativeCanvas.save()
                nativeCanvas.translate(offset.x, offset.y)
                // Pivot scaling around container center
                nativeCanvas.scale(scale, scale, canvasW / 2f, canvasH / 2f)

                // Render grid cells
                for (cell in kit.cells) {
                    val cxLeft = startX + cell.x * cellSize
                    val cxTop = startY + cell.y * cellSize

                    // Basic bounds calculation
                    val rectLeft = cxLeft
                    val rectTop = cxTop
                    val rectRight = cxLeft + cellSize
                    val rectBottom = cxTop + cellSize

                    // Optimize: Avoid drawing if cell is far outside the view boundaries
                    // (Simple viewport clipping based on coordinate systems)
                    // At high scales, viewport clipping saves substantial calculations

                    // Is there any highlighting?
                    val hasHighlight = highlightedColorLabel != null
                    val isHighlighted = cell.label == highlightedColorLabel
                    
                    // Determine primary color of this diamond
                    val kitColor = cell.quantizedColorValue
                    
                    // Apply visual dimming of unselected diamond colors for better focus
                    val finalCellColor = if (hasHighlight) {
                        if (isHighlighted) {
                            kitColor
                        } else {
                            // Dim the unselected color intensely to 15% opacity
                            val alpha = 25 // Out of 255
                            val r = android.graphics.Color.red(kitColor)
                            val g = android.graphics.Color.green(kitColor)
                            val b = android.graphics.Color.blue(kitColor)
                            android.graphics.Color.argb(alpha, r, g, b)
                        }
                    } else {
                        kitColor
                    }

                    // A background color for "Letter Only" mode (empty off-white canvas)
                    val letterOnlyBg = if (hasHighlight) {
                        if (isHighlighted) {
                            android.graphics.Color.WHITE
                        } else {
                            // Dimmed empty cell
                            android.graphics.Color.parseColor("#15222222")
                        }
                    } else {
                        android.graphics.Color.parseColor("#FFFDF9")
                    }

                    // 1. Draw diamond base cell
                    val fillPaint = Paint().apply {
                        isAntiAlias = true
                        style = Paint.Style.FILL
                    }

                    when (viewMode) {
                        ViewMode.COLOR_ONLY -> {
                            fillPaint.color = finalCellColor
                            // Render diamonds with rounded corners or 3D circular stone effect!
                            val rMargin = cellSize * 0.08f
                            nativeCanvas.drawRoundRect(
                                rectLeft + rMargin,
                                rectTop + rMargin,
                                rectRight - rMargin,
                                rectBottom - rMargin,
                                cellSize * 0.25f,
                                cellSize * 0.25f,
                                fillPaint
                            )

                            // Draw neat glowing facet reflection dot inside
                            val shineSize = cellSize * 0.12f
                            val shinePaint = Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.argb(
                                    if (hasHighlight && !isHighlighted) 35 else 180,
                                    255, 255, 255
                                )
                                style = Paint.Style.FILL
                            }
                            nativeCanvas.drawCircle(
                                rectLeft + cellSize * 0.3f,
                                rectTop + cellSize * 0.3f,
                                shineSize,
                                shinePaint
                            )
                        }

                        ViewMode.COLOR_WITH_LETTER -> {
                            fillPaint.color = finalCellColor
                            val rMargin = cellSize * 0.05f
                            nativeCanvas.drawRoundRect(
                                rectLeft + rMargin,
                                rectTop + rMargin,
                                rectRight - rMargin,
                                rectBottom - rMargin,
                                cellSize * 0.15f,
                                cellSize * 0.15f,
                                fillPaint
                            )

                            // Draw a small 3D facet ring around selected color
                            if (isHighlighted) {
                                val borderPaint = Paint().apply {
                                    isAntiAlias = true
                                    style = Paint.Style.STROKE
                                    strokeWidth = cellSize * 0.1f
                                    color = android.graphics.Color.YELLOW
                                }
                                nativeCanvas.drawRoundRect(
                                    rectLeft + rMargin,
                                    rectTop + rMargin,
                                    rectRight - rMargin,
                                    rectBottom - rMargin,
                                    cellSize * 0.15f,
                                    cellSize * 0.15f,
                                    borderPaint
                                )
                            }

                            // Render letter on top if scale is reasonable
                            if (currentCellSize > 12f) {
                                // Dynamic contrast color (black or white)
                                val baseColor = if (hasHighlight && !isHighlighted) {
                                    android.graphics.Color.argb(45, 120, 120, 120)
                                } else {
                                    val r = android.graphics.Color.red(kitColor)
                                    val g = android.graphics.Color.green(kitColor)
                                    val b = android.graphics.Color.blue(kitColor)
                                    val luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                                    if (luminance > 0.55) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                                }
                                textPaint.color = baseColor
                                textPaint.textSize = cellSize * 0.62f

                                // Vertical centering formula
                                val yPos = rectTop + (cellSize / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
                                nativeCanvas.drawText(cell.label, rectLeft + (cellSize / 2f), yPos, textPaint)
                            }
                        }

                        ViewMode.LETTER_ONLY -> {
                            // Draw light grey box background
                            fillPaint.color = letterOnlyBg
                            nativeCanvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, fillPaint)

                            // Thin cell divider lines for clarity in blueprint symbol view
                            val linePaint = Paint().apply {
                                style = Paint.Style.STROKE
                                strokeWidth = cellSize * 0.04f
                                color = android.graphics.Color.parseColor(
                                    if (hasHighlight && !isHighlighted) "#0A000000" else "#20000000"
                                )
                            }
                            nativeCanvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, linePaint)

                            // Render letter on top
                            if (currentCellSize > 10f) {
                                textPaint.color = if (hasHighlight && !isHighlighted) {
                                    android.graphics.Color.parseColor("#1F333333")
                                } else {
                                    android.graphics.Color.parseColor("#DD212121")
                                }
                                textPaint.textSize = cellSize * 0.65f

                                val yPos = rectTop + (cellSize / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f)
                                nativeCanvas.drawText(cell.label, rectLeft + (cellSize / 2f), yPos, textPaint)
                            }
                        }
                    }
                }

                // Restore translation matrices
                nativeCanvas.restore()
            }
        }
    }
}
