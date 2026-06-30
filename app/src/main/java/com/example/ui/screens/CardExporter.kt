package com.example.ui.screens

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream

object CardExporter {

    private fun getThemeColor(theme: String): Int {
        return when (theme) {
            "ROYAL_COBALT" -> 0xFF0F398A.toInt()
            "GOLDEN_ELITE" -> 0xFF8C6239.toInt()
            "EMERALD_GREEN" -> 0xFF136243.toInt()
            "OCEAN_SAPPHIRE" -> 0xFF00ACC1.toInt()
            "LUXURY_SLATE" -> 0xFF0F172A.toInt() // Visiting card matches luxury slate bg
            "CHAMPA_GOLD" -> 0xFF452D08.toInt()
            "ROYAL_PURPLE" -> 0xFF311B92.toInt()
            "NATURE_GREEN" -> 0xFF1B5E20.toInt()
            else -> 0xFFC62828.toInt() // CRIMSOM_GLOW / Default
        }
    }

    private fun drawCircularBitmap(canvas: Canvas, bitmap: Bitmap, cx: Float, cy: Float, radius: Float) {
        val size = (radius * 2).toInt()
        if (size <= 0) return
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val outCanvas = Canvas(output)
        
        val paint = Paint().apply {
            isAntiAlias = true
        }
        outCanvas.drawCircle(radius, radius, radius, paint)
        
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        outCanvas.drawBitmap(scaledBitmap, 0f, 0f, paint)
        
        canvas.drawBitmap(output, cx - radius, cy - radius, null)
    }

    // Modern ID Card Generator
    fun drawStudentIdCard(
        context: Context,
        student: Student,
        batchName: String,
        theme: String,
        academyName: String,
        address: String,
        phone: String,
        photoUri: android.net.Uri? = null,
        signatureUri: android.net.Uri? = null
    ): Bitmap {
        val width = 600
        val height = 955
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // White background
        canvas.drawColor(Color.WHITE)

        val themeColor = getThemeColor(theme)

        // Draw card border
        val borderPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        val cardRect = RectF(12f, 12f, width - 12f, height - 12f)
        canvas.drawRoundRect(cardRect, 24f, 24f, borderPaint)

        // Draw header background
        val headerPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val headerRect = RectF(15f, 15f, width - 15f, 160f)
        canvas.drawRect(headerRect, headerPaint)

        // Header Academy Name
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(academyName.uppercase(), width / 2f, 75f, titlePaint)

        // Header Subtitle
        val subTitlePaint = Paint().apply {
            color = Color.WHITE
            alpha = 200
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("ACADEMY SCHOLAR SECURE PROFILE PASS", width / 2f, 120f, subTitlePaint)

        // Student Profile picture area - Left column
        val avatarCenterX = 135f
        val avatarCenterY = 270f
        val avatarRadius = 65f
        
        var photoDrawn = false
        if (photoUri != null) {
            try {
                context.contentResolver.openInputStream(photoUri).use { inputStream ->
                    val rawBmp = BitmapFactory.decodeStream(inputStream)
                    if (rawBmp != null) {
                        drawCircularBitmap(canvas, rawBmp, avatarCenterX, avatarCenterY, avatarRadius)
                        photoDrawn = true
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CardExporter", "Failed to decode photoUri", e)
            }
        }

        if (!photoDrawn) {
            val avatarPaint = Paint().apply {
                color = themeColor
                alpha = 30 // light background
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarPaint)

            val avatarBorderPaint = Paint().apply {
                color = themeColor
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarBorderPaint)

            val initPaint = Paint().apply {
                color = themeColor
                textSize = 46f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val initials = student.name.split(" ").filter { it.isNotEmpty() }.take(2).map { it.first().uppercase() }.joinToString("")
            canvas.drawText(initials.ifEmpty { "ST" }, avatarCenterX, avatarCenterY + 16f, initPaint)
        }

        // STUDENT tag pill below photo
        val pillPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val pillRect = RectF(avatarCenterX - 65f, avatarCenterY + 80f, avatarCenterX + 65f, avatarCenterY + 115f)
        canvas.drawRoundRect(pillRect, 8f, 8f, pillPaint)

        val pillTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("STUDENT", avatarCenterX, avatarCenterY + 103f, pillTextPaint)

        // Signature Box below the photo & pill on the left column
        val sigBoxRect = RectF(avatarCenterX - 65f, avatarCenterY + 130f, avatarCenterX + 65f, avatarCenterY + 200f)
        val sigOutlinePaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val sigBgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(sigBoxRect, 8f, 8f, sigBgPaint)
        canvas.drawRoundRect(sigBoxRect, 8f, 8f, sigOutlinePaint)

        var sigDrawn = false
        if (signatureUri != null) {
            try {
                context.contentResolver.openInputStream(signatureUri).use { inputStream ->
                    val rawBmp = BitmapFactory.decodeStream(inputStream)
                    if (rawBmp != null) {
                        val pad = 6f
                        val destRect = RectF(sigBoxRect.left + pad, sigBoxRect.top + pad, sigBoxRect.right - pad, sigBoxRect.bottom - pad)
                        canvas.drawBitmap(rawBmp, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG).apply { isAntiAlias = true })
                        sigDrawn = true
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CardExporter", "Failed to decode signatureUri", e)
            }
        }

        if (!sigDrawn) {
            val sigTextPaint = Paint().apply {
                color = Color.GRAY
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("SIGNATURE", avatarCenterX, avatarCenterY + 171f, sigTextPaint)
        }

        // Student details list on the right column
        val textX = 265f
        var textY = 245f
        val detailsPaint = Paint().apply {
            color = Color.BLACK
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        fun drawDetailRow(label: String, value: String) {
            canvas.drawText(label, textX, textY, detailsPaint)
            // Limit length of value to prevent overlap
            val cleanValue = if (value.length > 20) value.take(18) + ".." else value
            canvas.drawText(cleanValue, textX + 110f, textY, valuePaint)
            textY += 38f
        }

        drawDetailRow("Name:", student.name)
        drawDetailRow("Roll ID:", student.rollNumber)
        drawDetailRow("Batch:", batchName)
        drawDetailRow("Class:", student.studentClass.ifEmpty { "X-Elite" })
        drawDetailRow("Parent:", student.parentName)
        drawDetailRow("Phone:", student.parentPhone)

        // Draw line separator
        val linePaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 2f
        }
        canvas.drawLine(40f, 510f, width - 40f, 510f, linePaint)

        // QR Code drawing (canvas vector)
        val qrSize = 150f
        val qrLeft = (width - qrSize) / 2f
        val qrTop = 575f

        val qrPaint = Paint().apply {
            color = themeColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Draw simulated QR pixel elements inside the QR region
        val pixelCount = 13
        val qrPixelSize = qrSize / pixelCount
        for (i in 0 until pixelCount) {
            for (j in 0 until pixelCount) {
                // Skip corner anchors
                if ((i < 4 && j < 4) || (i >= pixelCount - 4 && j < 4) || (i < 4 && j >= pixelCount - 4)) {
                    continue
                }
                val isFilled = ((i * 7 + j * 13) % 4 == 0) || ((i + j) % 3 == 0)
                if (isFilled) {
                    canvas.drawRect(
                        qrLeft + i * qrPixelSize,
                        qrTop + j * qrPixelSize,
                        qrLeft + (i + 1) * qrPixelSize,
                        qrTop + (j + 1) * qrPixelSize,
                        qrPaint
                    )
                }
            }
        }

        // Helper to draw QR anchors (Finder patterns)
        fun drawAnchor(xIdx: Int, yIdx: Int) {
            val px = qrLeft + xIdx * qrPixelSize
            val py = qrTop + yIdx * qrPixelSize
            
            // Outer 4x4
            canvas.drawRect(px, py, px + qrPixelSize * 4, py + qrPixelSize * 4, qrPaint)
            // White inner 2x2
            val whitePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(px + qrPixelSize, py + qrPixelSize, px + qrPixelSize * 3, py + qrPixelSize * 3, whitePaint)
            // Center 1.5x1.5
            canvas.drawRect(px + qrPixelSize * 1.25f, py + qrPixelSize * 1.25f, px + qrPixelSize * 2.75f, py + qrPixelSize * 2.75f, qrPaint)
        }

        drawAnchor(0, 0)
        drawAnchor(pixelCount - 4, 0)
        drawAnchor(0, pixelCount - 4)

        val qrLabelPaint = Paint().apply {
            color = themeColor
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("SCAN FOR DIGITAL ATTENDANCE & LIBRARY LOGS", width / 2f, 550f, qrLabelPaint)

        // Bottom Strip
        val footerPaint = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            style = Paint.Style.FILL
        }
        canvas.drawRect(15f, 805f, width - 15f, height - 15f, footerPaint)

        val footerTextPaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Approved Campus: $address", 30f, 850f, footerTextPaint)
        
        val lowerAcademy = academyName.lowercase()
        val isEnrolled = lowerAcademy.isNotEmpty() && 
            !lowerAcademy.contains("my application") && 
            !lowerAcademy.contains("untitled") && (
                lowerAcademy.contains("coaching") ||
                lowerAcademy.contains("tuition") ||
                lowerAcademy.contains("tution") ||
                lowerAcademy.contains("teacher") ||
                lowerAcademy.contains("tutor") ||
                lowerAcademy.contains("library") ||
                lowerAcademy.contains("classes") ||
                lowerAcademy.contains("institute") ||
                lowerAcademy.contains("academy") ||
                lowerAcademy.contains("school") ||
                lowerAcademy.contains("center") ||
                lowerAcademy.contains("centre") ||
                lowerAcademy.contains("study") ||
                lowerAcademy.contains("verma") ||
                lowerAcademy.contains("city") ||
                lowerAcademy.contains("silent") ||
                lowerAcademy.contains("aspirant") ||
                lowerAcademy.contains("success")
            )
        val helplinePhone = if (isEnrolled) phone else ""
        canvas.drawText("Helpline Desk: $helplinePhone", 30f, 885f, footerTextPaint)

        // Secure ID Badge
        val badgePaint = Paint().apply {
            color = themeColor
            alpha = 30
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val badgeRect = RectF(width - 200f, 845f, width - 30f, 895f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, badgePaint)

        val badgeTextPaint = Paint().apply {
            color = themeColor
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("SECURE VERIFIED ID", width - 115f, 876f, badgeTextPaint)

        return bitmap
    }

    private fun drawWordWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        paint: Paint,
        maxWidth: Float,
        lineHeight: Float
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var y = startY
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth) {
                canvas.drawText(currentLine, x, y, paint)
                y += lineHeight
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, y, paint)
            y += lineHeight
        }
        return y
    }

    // Modern Business Visiting Card Generator - High Fidelity Match With Screen Previews
    fun drawVisitingCard(
        context: Context,
        directorName: String,
        academyName: String,
        phone: String,
        email: String,
        address: String,
        theme: String,
        userRole: String = "GUEST",
        studentName: String? = null,
        studentRoll: String? = null,
        studentClass: String? = null,
        photoUri: android.net.Uri? = null
    ): Bitmap {
        val width = 1000
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Map theme strings to precise backgrounds to match on-screen preview
        val activeBg = when (theme) {
            "LUXURY_SLATE" -> 0xFF0F172A.toInt()
            "CHAMPA_GOLD" -> 0xFF452D08.toInt()
            "ROYAL_PURPLE" -> 0xFF311B92.toInt()
            "NATURE_GREEN" -> 0xFF1B5E20.toInt()
            else -> 0xFF0C4A6E.toInt() // Ocean Breeze / default
        }
        canvas.drawColor(activeBg)

        // 1. Draw top Header portion (analogous to Row)
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(academyName.uppercase(), 50f, 85f, titlePaint)

        val subtitlePaint = Paint().apply {
            color = Color.WHITE
            alpha = 153 // 0.6 alpha
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("ELITE PRIVATE COACHING CENTRE", 50f, 125f, subtitlePaint)

        // Draw school icon or cap in the top right
        val iconPaint = Paint().apply {
            color = if (theme == "CHAMPA_GOLD") Color.parseColor("#FBBF24") else Color.parseColor("#38BDF8")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Let's draw a nice stylized school symbol or star/cap on the top right
        val path = Path().apply {
            moveTo(910f, 50f)
            lineTo(930f, 75f)
            lineTo(910f, 100f)
            lineTo(890f, 75f)
            close()
        }
        canvas.drawPath(path, iconPaint)

        // 2. Draw Bottom Row portion containing avatar on left, contact info on right
        val avatarCenterX = 110f
        val avatarCenterY = 400f
        val avatarRadius = 55f

        // Draw Avatar circular container
        var avatarDrawn = false
        if (photoUri != null) {
            try {
                context.contentResolver.openInputStream(photoUri).use { inputStream ->
                    val rawBmp = BitmapFactory.decodeStream(inputStream)
                    if (rawBmp != null) {
                        drawCircularBitmap(canvas, rawBmp, avatarCenterX, avatarCenterY, avatarRadius)
                        avatarDrawn = true
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CardExporter", "Failed to decode visiting card photoUri", e)
            }
        }

        if (!avatarDrawn) {
            // Draw stylized circle avatar background
            val avatarBgPaint = Paint().apply {
                color = Color.WHITE
                alpha = 38 // 0.15 alpha
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarBgPaint)

            val avatarBorderPaint = Paint().apply {
                color = Color.WHITE
                alpha = 127 // 0.5 alpha
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            canvas.drawCircle(avatarCenterX, avatarCenterY, avatarRadius, avatarBorderPaint)

            // Draw letter initials or a simple symbol
            val avatarTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 38f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val initial = if (userRole == "STUDENT" && studentName != null) {
                studentName.take(1).uppercase()
            } else {
                directorName.take(1).uppercase()
            }
            canvas.drawText(initial.ifEmpty { "A" }, avatarCenterX, avatarCenterY + 14f, avatarTextPaint)
        }

        // Draw profile info next to the avatar (at X = 200f)
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val desigPaint = Paint().apply {
            color = Color.parseColor("#38BDF8")
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        if (userRole == "STUDENT" && studentName != null) {
            canvas.drawText(studentName, 190f, 385f, namePaint)
            canvas.drawText("Student Roll ID: ${studentRoll ?: "N/A"}", 190f, 420f, desigPaint)
            
            // Student Class details below
            val classPaint = Paint().apply {
                color = Color.GRAY
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("STANDARD / SEC: ${studentClass ?: "N/A"}", 190f, 455f, classPaint)
        } else {
            canvas.drawText(directorName, 190f, 385f, namePaint)
            canvas.drawText("Managing Director & Faculty", 190f, 420f, desigPaint)
        }

        // Draw contact detail lines on the right (aligned to RIGHT, X = 950f)
        val rightTextPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 17f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText("📞 $phone", 950f, 360f, rightTextPaint)
        canvas.drawText("✉️ $email", 950f, 410f, rightTextPaint)
        
        // Address text wrapping or simple draw
        val addressPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val cleanAddress = if (address.length > 35) address.take(32) + "..." else address
        canvas.drawText("📍 $cleanAddress", 950f, 460f, addressPaint)

        // Bottom Strip Network Badges
        val verifyPaint1 = Paint().apply {
            color = Color.parseColor("#34D399") // Green
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TAMS ACADEMY NETWORK CERTIFIED", 50f, 545f, verifyPaint1)

        val verifyPaint2 = Paint().apply {
            color = Color.parseColor("#60A5FA") // Blue
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("SECURE VERIFIED", 950f, 545f, verifyPaint2)

        return bitmap
    }

    // Modern QR Standee Generator - High Fidelity Match With GraphicalAcademyQrView Screen Preview
    fun drawQrStandee(
        context: Context,
        academyName: String,
        merchantName: String,
        merchantUpiId: String
    ): Bitmap {
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background Color(0xFF0F172A)
        val darkSlate = 0xFF0F172A.toInt()
        canvas.drawColor(darkSlate)

        // Border: 4.dp equivalent BorderStroke(1.5.dp, Color(0xFF818CF8))
        val borderPaint = Paint().apply {
            color = Color.parseColor("#818CF8")
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(25f, 25f, width - 25f, height - 25f), 32f, 32f, borderPaint)

        // Content layout
        // Text 1: OFFICIAL REGISTRATION SCANNER
        val tagPaint = Paint().apply {
            color = Color.parseColor("#818CF8")
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            letterSpacing = 0.15f
        }
        canvas.drawText("OFFICIAL REGISTRATION SCANNER", width / 2f, 110f, tagPaint)

        // Text 2: Academy Name
        val namePaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(academyName.uppercase(), width / 2f, 175f, namePaint)

        // White QR Box Canvas
        val boxSize = 440f
        val boxX = width / 2f
        val boxY = 480f
        val qrBoxRect = RectF(boxX - boxSize/2f, boxY - boxSize/2f, boxX + boxSize/2f, boxY + boxSize/2f)

        val qrBoxPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Draw the white rounded square preview container
        canvas.drawRoundRect(qrBoxRect, 24f, 24f, qrBoxPaint)

        // QR Vector pixels inside
        val qrLeft = boxX - boxSize/2f + 35f
        val qrTop = boxY - boxSize/2f + 35f
        val qrSize = boxSize - 70f

        val qrPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cols = 21
        val cellSize = qrSize / cols

        // Draw standard simulated QR content pixels
        for (i in 0 until cols) {
            for (j in 0 until cols) {
                // Skip corner anchors area
                if ((i < 8 && j < 8) || (i > cols - 9 && j < 8) || (i < 8 && j > cols - 9)) continue

                val hash = (i * 37 + j * 79) % 11
                if (hash == 1 || hash == 3 || hash == 7 || hash == 9) {
                    canvas.drawRect(
                        qrLeft + i * cellSize,
                        qrTop + j * cellSize,
                        qrLeft + (i + 1) * cellSize,
                        qrTop + (j + 1) * cellSize,
                        qrPaint
                    )
                }
            }
        }

        // Anchor finder patterns helper
        fun drawAnchor(xIdx: Int, yIdx: Int) {
            val px = qrLeft + xIdx * cellSize
            val py = qrTop + yIdx * cellSize

            // Outer black ring (7x7 cells)
            canvas.drawRect(px, py, px + cellSize * 7, py + cellSize * 7, qrPaint)
            // Inner white ring (5x5 cells)
            val whitePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            canvas.drawRect(px + cellSize, py + cellSize, px + cellSize * 6, py + cellSize * 6, whitePaint)
            // Center black dot (3x3 cells)
            canvas.drawRect(px + cellSize * 2, py + cellSize * 2, px + cellSize * 5, py + cellSize * 5, qrPaint)
        }

        drawAnchor(0, 0)
        drawAnchor(cols - 7, 0)
        drawAnchor(0, cols - 7)

        // Small circle at center of the QR Code matching the live preview logo
        val centerCirclePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(boxX, boxY, 34f, centerCirclePaint)

        val centerBorderPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            isAntiAlias = true
        }
        canvas.drawCircle(boxX, boxY, 34f, centerBorderPaint)

        val centerTextPaint = Paint().apply {
            color = Color.parseColor("#4F46E5")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val centerChar = academyName.take(1).uppercase()
        canvas.drawText(centerChar, boxX, boxY + 8f, centerTextPaint)

        // UPI Merchant ID title text below QR code container
        val upiLabelPaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("UPI Merchant: $merchantUpiId", width / 2f, 775f, upiLabelPaint)

        // Descriptive footer instruction text word-wrapped nicely
        val footerDescPaint = Paint().apply {
            color = Color.parseColor("#64748B") // slate gray
            textSize = 17f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val descStr = "Students scan QR inside WhatsApp or phone camera to establish direct center connections for fees, syllabus, and admissions."
        drawWordWrappedText(canvas, descStr, width / 2f, 835f, footerDescPaint, 660f, 26f)

        // High quality bhim upi branding strip at the absolute bottom
        val bhimTextPaint = Paint().apply {
            color = Color.GRAY
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Verified Merchant • Direct Bank Core UPI gateway", width / 2f, 1130f, bhimTextPaint)

        return bitmap
    }

    // High efficiency download logic returning path description to show the user
    fun saveAndDownloadBitmap(
        context: Context,
        bitmap: Bitmap,
        baseName: String
    ): String {
        val fileName = "${baseName}.jpg"
        val savedFile = saveBitmapToPublicStorage(context, bitmap, fileName)
        
        // Also save as PDF format immediately so they can download as PDF as requested!
        val pdfName = "${baseName}.pdf"
        val savedPdf = saveBitmapAsPdf(context, bitmap, pdfName)

        val resultMsg = if (savedFile != null && savedPdf != null) {
            "Real high-res JPG & PDF successfully generated!\n\n" +
            "📍 JPG: Pictures/AcademyCards/$fileName\n" +
            "📍 PDF: Downloads/$pdfName"
        } else if (savedFile != null) {
            "Saved successfully as JPG: Pictures/AcademyCards/$fileName."
        } else if (savedPdf != null) {
            "Saved successfully as PDF: Downloads/$pdfName."
        } else {
            "Saved dynamically to internal storage path."
        }
        return resultMsg
    }

    // Securely share a Bitmap as a JPEG file via FileProvider
    fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        baseName: String,
        text: String
    ) {
        try {
            val cachePath = File(context.cacheDir, "shared_images")
            cachePath.mkdirs()
            val file = File(cachePath, "${baseName}.jpg")
            val stream = java.io.FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()

            val contentUri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.example.fileprovider",
                file
            )

            if (contentUri != null) {
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(android.content.Intent.EXTRA_STREAM, contentUri)
                    putExtra(android.content.Intent.EXTRA_TEXT, text)
                    clipData = android.content.ClipData.newRawUri("", contentUri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(android.content.Intent.createChooser(intent, "Share Image via WhatsApp"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(context, "Sharing failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBitmapToPublicStorage(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): File? {
        val resolver = context.contentResolver
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val contentValues = android.content.ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AcademyCards")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val imageUri = resolver.insert(imageCollection, contentValues)
                if (imageUri != null) {
                    resolver.openOutputStream(imageUri).use { out ->
                        if (out != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Copy directly to standard Downloads folder
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            // Sandbox fallback
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                if (!fallbackDir.exists()) {
                    fallbackDir.mkdirs()
                }
                val file = File(fallbackDir, fileName)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                return file
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return null
    }

    fun saveBitmapAsPdf(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): File? {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)
        
        val maxWidth = 515f
        val maxHeight = 762f
        val scale = Math.min(maxWidth / bitmap.width, maxHeight / bitmap.height)
        val drawWidth = bitmap.width * scale
        val drawHeight = bitmap.height * scale
        val dx = (595f - drawWidth) / 2f
        val dy = (842f - drawHeight) / 2f
        
        val rectF = RectF(dx, dy, dx + drawWidth, dy + drawHeight)
        canvas.drawBitmap(bitmap, null, rectF, Paint(Paint.FILTER_BITMAP_FLAG))
        
        pdfDocument.finishPage(page)
        
        var targetFile: File? = null
        try {
            val baseDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!baseDownloads.exists()) {
                baseDownloads.mkdirs()
            }
            targetFile = File(baseDownloads, fileName)
            FileOutputStream(targetFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                if (!fallbackDir.exists()) {
                    fallbackDir.mkdirs()
                }
                targetFile = File(fallbackDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    pdfDocument.writeTo(out)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } finally {
            pdfDocument.close()
        }
        return targetFile
    }

    fun generatePaymentInvoicePdf(
        context: Context,
        payment: com.example.data.model.FeePayment,
        student: Student,
        batch: com.example.data.model.Batch?,
        academyName: String,
        address: String,
        phone: String,
        email: String
    ): String {
        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, 595f, 842f, bgPaint)

        // Header Background block
        val headerBgPaint = Paint().apply {
            color = Color.parseColor("#0F398A") // Cobalt Blue
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(20f, 20f, 575f, 130f, headerBgPaint)

        // Academy Name
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(academyName.uppercase(), 40f, 65f, titlePaint)

        // Academy Subtitle / Contact details
        val subPaint = Paint().apply {
            color = Color.WHITE
            alpha = 200
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("📍 $address", 40f, 88f, subPaint)
        canvas.drawText("📞 Phone: $phone  |  ✉️ Email: $email", 40f, 108f, subPaint)

        // Invoice Title
        val invTitlePaint = Paint().apply {
            color = Color.parseColor("#0F398A")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("TAX INVOICE / RECEIPT", 40f, 175f, invTitlePaint)

        // Invoice details on the right
        val detailsPaint = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
        val dateStr = sdf.format(java.util.Date(payment.datePaid))

        canvas.drawText("Receipt No : ${payment.receiptNo.ifEmpty { "REC-${payment.id}" }}", 380f, 165f, detailsPaint)
        canvas.drawText("Date Paid  : $dateStr", 380f, 185f, detailsPaint)
        canvas.drawText("Status     : SUCCESS (REALTIME)", 380f, 205f, detailsPaint)

        // Draw a light gray divider
        val divPaint = Paint().apply {
            color = Color.parseColor("#E5E7EB")
            strokeWidth = 1.5f
        }
        canvas.drawLine(20f, 225f, 575f, 225f, divPaint)

        // BILL TO Section
        val sectHeaderPaint = Paint().apply {
            color = Color.parseColor("#374151")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("BILL TO (STUDENT & GUARDIAN DETAILS)", 40f, 255f, sectHeaderPaint)

        // Bill to details
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#1F2937")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val labelPaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Student Name:", 40f, 285f, labelPaint)
        canvas.drawText(student.name.uppercase(), 140f, 285f, bodyPaint)

        canvas.drawText("Roll Number:", 40f, 305f, labelPaint)
        canvas.drawText(student.rollNumber, 140f, 305f, bodyPaint)

        canvas.drawText("Batch/Class:", 40f, 325f, labelPaint)
        canvas.drawText("${batch?.name ?: "General"} | ${student.studentClass.ifEmpty { "X-Elite" }}", 140f, 325f, bodyPaint)

        canvas.drawText("Guardian Name:", 40f, 345f, labelPaint)
        canvas.drawText(student.parentName, 140f, 345f, bodyPaint)

        canvas.drawText("Guardian Phone:", 40f, 365f, labelPaint)
        canvas.drawText(student.parentPhone, 140f, 365f, bodyPaint)

        // Draw Table headers at Y = 405
        val tableHeaderBgPaint = Paint().apply {
            color = Color.parseColor("#F3F4F6")
            style = Paint.Style.FILL
        }
        canvas.drawRect(20f, 405f, 575f, 435f, tableHeaderBgPaint)

        val tableHeaderPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("FEE PARTICULAR DESCRIPTION", 40f, 424f, tableHeaderPaint)
        canvas.drawText("RATE", 320f, 424f, tableHeaderPaint)
        canvas.drawText("WAIVER / DISC", 410f, 424f, tableHeaderPaint)
        canvas.drawText("NET AMOUNT", 495f, 424f, tableHeaderPaint)

        // Draw Row
        val tableRowPaint = Paint().apply {
            color = Color.parseColor("#374151")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val rateVal = payment.amountPaid + payment.discount
        canvas.drawText("Tuition fee installment for month: ${payment.monthPaidFor}", 40f, 465f, tableRowPaint)
        canvas.drawText("₹${rateVal.toInt()}", 320f, 465f, tableRowPaint)
        canvas.drawText("₹${payment.discount.toInt()}", 410f, 465f, tableRowPaint)
        canvas.drawText("₹${payment.amountPaid.toInt()}", 495f, 465f, tableRowPaint)

        // Divider
        canvas.drawLine(20f, 490f, 575f, 490f, divPaint)

        // Totals Box on Right
        val totalLabelPaint = Paint().apply {
            color = Color.parseColor("#4B5563")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val totalValPaint = Paint().apply {
            color = Color.parseColor("#111827")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val netGrandPaint = Paint().apply {
            color = Color.parseColor("#0F398A")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Subtotal:", 350f, 520f, totalLabelPaint)
        canvas.drawText("₹${rateVal.toInt()}", 495f, 520f, totalValPaint)

        canvas.drawText("Total Waivers & Discounts:", 350f, 540f, totalLabelPaint)
        canvas.drawText("- ₹${payment.discount.toInt()}", 495f, 540f, totalValPaint)

        canvas.drawText("Net Paid (Settled):", 350f, 565f, totalLabelPaint)
        canvas.drawText("₹${payment.amountPaid.toInt()}", 495f, 565f, netGrandPaint)

        // Payment info card
        val infoCardBgPaint = Paint().apply {
            color = Color.parseColor("#ECFDF5")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(40f, 505f, 300f, 575f), 6f, 6f, infoCardBgPaint)

        val infoTitlePaint = Paint().apply {
            color = Color.parseColor("#065F46")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val infoTextPaint = Paint().apply {
            color = Color.parseColor("#047857")
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("PAYMENT METHOD DETAIL", 52f, 523f, infoTitlePaint)
        canvas.drawText("Channel Mode  : ${payment.paymentMode}", 52f, 540f, infoTextPaint)
        canvas.drawText("Settle Status : FULLY SETTLED REALTIME", 52f, 555f, infoTextPaint)

        // Footer note
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("This is an authenticated ledger voucher generated dynamically from the TAMS database client.", 40f, 750f, footerPaint)
        canvas.drawText("For queries, contact support or mail: $email", 40f, 765f, footerPaint)

        // Stamp and Signature
        val stampBorderPaint = Paint().apply {
            color = Color.parseColor("#0F398A")
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val stampTextPaint = Paint().apply {
            color = Color.parseColor("#0F398A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(400f, 700f, 520f, 740f), 4f, 4f, stampBorderPaint)
        canvas.drawText("TAMS SECURE", 418f, 718f, stampTextPaint)
        canvas.drawText("VERIFIED LOG", 421f, 732f, stampTextPaint)

        pdfDocument.finishPage(page)

        val fileName = "Invoice_Receipt_${payment.receiptNo.ifEmpty { "REC_" + payment.id }}.pdf"
        var targetFile: File? = null
        try {
            val baseDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!baseDownloads.exists()) {
                baseDownloads.mkdirs()
            }
            targetFile = File(baseDownloads, fileName)
            FileOutputStream(targetFile).use { out ->
                pdfDocument.writeTo(out)
            }
        } catch (e: Exception) {
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                if (!fallbackDir.exists()) {
                    fallbackDir.mkdirs()
                }
                targetFile = File(fallbackDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    pdfDocument.writeTo(out)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } finally {
            pdfDocument.close()
        }

        return if (targetFile != null) {
            "PDF Invoice saved successfully!\n📍 Saved to: Downloads/$fileName"
        } else {
            "Voucher receipt processed successfully in memory."
        }
    }
}
