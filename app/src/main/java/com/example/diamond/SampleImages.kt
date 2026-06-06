package com.example.diamond

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader

object SampleImages {

    enum class SampleType(val label: String, val description: String) {
        SUNFLOWER("Sunny Sunflower", "Vibrant floral art with high-contrast colors"),
        SUNSET("Ocean Sunset", "Rich warm gradients and horizontal wave bands"),
        MANDALA("Mystic Mandala", "Geometric concentric patterns with bold palettes"),
        MOUNTAIN("Cozy Cabin & Pine", "Misty grayscale and silhouette forest landscape")
    }

    fun generate(type: SampleType, width: Int = 200, height: Int = 200): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (type) {
            SampleType.SUNFLOWER -> {
                // Background sky
                canvas.drawColor(Color.parseColor("#3B5998")) // Deep blue-teal

                // Leaves & stem
                paint.color = Color.parseColor("#2E7D32") // Dark green
                paint.strokeWidth = width * 0.08f
                canvas.drawLine(width * 0.5f, height * 0.5f, width * 0.5f, height * 0.95f, paint)

                paint.style = Paint.Style.FILL
                val leafPath1 = Path().apply {
                    moveTo(width * 0.5f, height * 0.7f)
                    quadTo(width * 0.2f, height * 0.6f, width * 0.15f, height * 0.75f)
                    quadTo(width * 0.35f, height * 0.85f, width * 0.5f, height * 0.8f)
                    close()
                }
                canvas.drawPath(leafPath1, paint)

                val leafPath2 = Path().apply {
                    moveTo(width * 0.5f, height * 0.75f)
                    quadTo(width * 0.8f, height * 0.65f, width * 0.85f, height * 0.82f)
                    quadTo(width * 0.65f, height * 0.9f, width * 0.5f, height * 0.85f)
                    close()
                }
                canvas.drawPath(leafPath2, paint)

                // Sun petals
                val petalCount = 14
                val cx = width * 0.5f
                val cy = height * 0.45f
                paint.color = Color.parseColor("#FFD54F") // Bright gold yellow
                val outerRadius = width * 0.35f
                val innerRadius = width * 0.15f

                for (i in 0 until petalCount) {
                    val angle = (2 * Math.PI * i) / petalCount
                    val nextAngle = (2 * Math.PI * (i + 0.5)) / petalCount
                    val endAngle = (2 * Math.PI * (i + 1)) / petalCount

                    val petalPath = Path().apply {
                        moveTo(cx, cy)
                        lineTo(
                            (cx + Math.cos(angle) * innerRadius).toFloat(),
                            (cy + Math.sin(angle) * innerRadius).toFloat()
                        )
                        quadTo(
                            (cx + Math.cos(nextAngle) * outerRadius).toFloat(),
                            (cy + Math.sin(nextAngle) * outerRadius).toFloat(),
                            (cx + Math.cos(endAngle) * innerRadius).toFloat(),
                            (cy + Math.sin(endAngle) * innerRadius).toFloat()
                        )
                        close()
                    }
                    canvas.drawPath(petalPath, paint)
                }

                // Dark inner core
                paint.color = Color.parseColor("#5D4037") // Dark brown
                canvas.drawCircle(cx, cy, width * 0.15f, paint)

                // Core seeds detail
                paint.color = Color.parseColor("#3E2723") // Extra dark brown
                canvas.drawCircle(cx, cy, width * 0.08f, paint)
            }

            SampleType.SUNSET -> {
                // Sky gradient
                val skyPaint = Paint().apply {
                    isAntiAlias = true
                    shader = RadialGradient(
                        width * 0.5f, height * 0.4f, width * 0.8f,
                        intArrayOf(Color.parseColor("#FFE082"), Color.parseColor("#FF7043"), Color.parseColor("#9C27B0")),
                        null, Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)

                // Yellow Sun
                paint.color = Color.parseColor("#FFFFEE")
                canvas.drawCircle(width * 0.5f, height * 0.45f, width * 0.12f, paint)

                // Ocean water bands (layered)
                // Bottom wave 1
                paint.color = Color.parseColor("#1A237E") // Deep violet blue
                canvas.drawRect(0f, height * 0.65f, width.toFloat(), height.toFloat(), paint)

                // Reflective waves
                paint.color = Color.parseColor("#D81B60") // Raspberry
                canvas.drawRect(0f, height * 0.6f, width.toFloat(), height * 0.65f, paint)

                // Reflective highlights on water
                paint.color = Color.parseColor("#FF5722") // Orange
                val paths = Path().apply {
                    moveTo(width * 0.3f, height * 0.6f)
                    lineTo(width * 0.7f, height * 0.6f)
                    lineTo(width * 0.6f, height * 0.65f)
                    lineTo(width * 0.4f, height * 0.65f)
                    close()
                }
                canvas.drawPath(paths, paint)

                paint.color = Color.parseColor("#FFCC00") // Yellow reflection
                canvas.drawRect(width * 0.45f, height * 0.6f, width * 0.55f, height * 0.62f, paint)
            }

            SampleType.MANDALA -> {
                canvas.drawColor(Color.parseColor("#121212")) // Slate background

                val cx = width * 0.5f
                val cy = height * 0.5f

                // Outer circles
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = width * 0.015f

                paint.color = Color.parseColor("#00E676") // Green
                canvas.drawCircle(cx, cy, width * 0.42f, paint)

                paint.color = Color.parseColor("#00B0FF") // Blue
                canvas.drawCircle(cx, cy, width * 0.34f, paint)

                paint.color = Color.parseColor("#E040FB") // Pink
                canvas.drawCircle(cx, cy, width * 0.26f, paint)

                // Inner filled design
                paint.style = Paint.Style.FILL

                // Concentric teeth
                val spokes = 12
                paint.color = Color.parseColor("#FF3D00") // Bright red orange
                for (i in 0 until spokes) {
                    val angle = (2 * Math.PI * i) / spokes
                    val x = (cx + Math.cos(angle) * width * 0.21f).toFloat()
                    val y = (cy + Math.sin(angle) * height * 0.21f).toFloat()
                    canvas.drawCircle(x, y, width * 0.05f, paint)
                }

                paint.color = Color.parseColor("#FFD600") // Bright golden yellow
                for (i in 0 until spokes) {
                    val angle = (2 * Math.PI * (i + 0.5)) / spokes
                    val x = (cx + Math.cos(angle) * width * 0.15f).toFloat()
                    val y = (cy + Math.sin(angle) * height * 0.15f).toFloat()
                    canvas.drawCircle(x, y, width * 0.04f, paint)
                }

                // Center core
                paint.color = Color.parseColor("#00E5FF") // Turquoise
                canvas.drawCircle(cx, cy, width * 0.08f, paint)

                paint.color = Color.parseColor("#FFFFFF") // White eye
                canvas.drawCircle(cx, cy, width * 0.03f, paint)
            }

            SampleType.MOUNTAIN -> {
                // Grayscale base
                // Foggy sky
                paint.style = Paint.Style.FILL
                val skyShader = RadialGradient(
                    width * 0.5f, 0f, height.toFloat(),
                    intArrayOf(Color.parseColor("#E0E0E0"), Color.parseColor("#9E9E9E"), Color.parseColor("#424242")),
                    null, Shader.TileMode.CLAMP
                )
                val skyPaint = Paint().apply {
                    isAntiAlias = true
                    shader = skyShader
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)

                // Distant foggy mountain (gray)
                paint.color = Color.parseColor("#757575")
                val mountain1 = Path().apply {
                    moveTo(0f, height * 0.7f)
                    lineTo(width * 0.35f, height * 0.4f)
                    lineTo(width * 0.7f, height * 0.65f)
                    lineTo(width.toFloat(), height * 0.5f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(mountain1, paint)

                // Foreground detailed mountain (dark gray)
                paint.color = Color.parseColor("#212121")
                val mountain2 = Path().apply {
                    moveTo(0f, height.toFloat())
                    lineTo(0f, height * 0.75f)
                    lineTo(width * 0.2f, height * 0.65f)
                    lineTo(width * 0.55f, height * 0.5f)
                    lineTo(width * 0.8f, height * 0.7f)
                    lineTo(width.toFloat(), height * 0.58f)
                    lineTo(width.toFloat(), height.toFloat())
                    close()
                }
                canvas.drawPath(mountain2, paint)

                // Cozy cabin silhouette outline
                paint.color = Color.parseColor("#0A0A0A") // Almost black
                val cabinPath = Path().apply {
                    moveTo(width * 0.55f, height * 0.85f)
                    lineTo(width * 0.55f, height * 0.75f) // Left wall
                    lineTo(width * 0.65f, height * 0.7f)  // Peak
                    lineTo(width * 0.75f, height * 0.75f) // Right roof joint
                    lineTo(width * 0.75f, height * 0.85f) // Right wall
                    close()
                }
                canvas.drawPath(cabinPath, paint)

                // Warm lit window (only yellow accent in the grayscale image)
                paint.color = Color.parseColor("#FFD54F")
                canvas.drawRect(width * 0.62f, height * 0.77f, width * 0.68f, height * 0.82f, paint)

                // Pine tree shapes
                paint.color = Color.parseColor("#151515")
                fun drawPine(tx: Float, ty: Float, tHeight: Float) {
                    val pinePath = Path().apply {
                        moveTo(tx, ty - tHeight)
                        lineTo(tx - tHeight * 0.35f, ty)
                        lineTo(tx + tHeight * 0.35f, ty)
                        close()
                    }
                    canvas.drawPath(pinePath, paint)
                }

                drawPine(width * 0.15f, height * 0.9f, height * 0.25f)
                drawPine(width * 0.28f, height * 0.95f, height * 0.18f)
                drawPine(width * 0.86f, height * 0.92f, height * 0.3f)
            }
        }

        return bitmap
    }
}
