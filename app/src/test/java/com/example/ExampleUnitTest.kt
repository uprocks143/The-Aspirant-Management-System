package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ExampleUnitTest {

    private fun sanitizeIndianPhone(phone: String): String {
        val raw = phone.replace(Regex("[^0-9]"), "")
        return when {
            raw.startsWith("91") && raw.length == 12 -> raw
            raw.startsWith("0") && raw.length == 11 -> "91" + raw.substring(1)
            raw.length == 10 -> "91$raw"
            else -> raw
        }
    }

    @Test
    fun testPhoneSanitization_handlesAllFormats() {
        // Standard 10-digit number
        assertEquals("919876543210", sanitizeIndianPhone("9876543210"))
        // Number with +91 country prefix
        assertEquals("919876543210", sanitizeIndianPhone("+91 98765 43210"))
        // Number with leading 0 (trunk prefix)
        assertEquals("919876543210", sanitizeIndianPhone("09876543210"))
        // Number already having 91
        assertEquals("919876543210", sanitizeIndianPhone("919876543210"))
        // Number with dashes
        assertEquals("919876543210", sanitizeIndianPhone("+91-9876-543-210"))
    }

    @Test
    fun testUpiPaymentLinks_formatValidity() {
        val upiId = "smtsharma282.sks@okaxis"
        val dueAmount = 1500
        val academy = "TAMS Academy"
        val student = "Aarav Sharma"

        val clickToPay = "https://upiqr.in/pay/$upiId/$dueAmount"
        assertTrue(clickToPay.startsWith("https://upiqr.in/pay/"))
        assertTrue(clickToPay.contains(upiId))
        assertTrue(clickToPay.endsWith("1500"))

        val upiDeepLink = "upi://pay?pa=$upiId&pn=${java.net.URLEncoder.encode(academy, "UTF-8")}&am=$dueAmount&cu=INR&tn=${java.net.URLEncoder.encode("Fee for $student", "UTF-8")}"
        assertTrue(upiDeepLink.startsWith("upi://pay?"))
        assertTrue(upiDeepLink.contains("pa=$upiId"))
        assertTrue(upiDeepLink.contains("am=1500"))
        assertTrue(upiDeepLink.contains("cu=INR"))
    }

    @Test
    fun testNoticeAudienceFiltering() {
        data class TestNotice(val id: Long, val title: String, val targetBatchId: Long, val isPinned: Boolean)

        val notices = listOf(
            TestNotice(1, "All batches holiday notice", 0L, true),
            TestNotice(2, "Batch 101 urgent exam schedule", 101L, true),
            TestNotice(3, "Batch 102 exam schedule", 102L, true),
            TestNotice(4, "All batches regular announcement", 0L, false)
        )

        // Student in Batch 101
        val batch101Pinned = notices.filter { it.isPinned && (it.targetBatchId == 0L || it.targetBatchId == 101L) }
        assertEquals(2, batch101Pinned.size)
        assertTrue(batch101Pinned.any { it.id == 1L })
        assertTrue(batch101Pinned.any { it.id == 2L })

        // Student in Batch 102
        val batch102Pinned = notices.filter { it.isPinned && (it.targetBatchId == 0L || it.targetBatchId == 102L) }
        assertEquals(2, batch102Pinned.size)
        assertTrue(batch102Pinned.any { it.id == 1L })
        assertTrue(batch102Pinned.any { it.id == 3L })
    }

    @Test
    fun addition_isCorrect() {
        val file = File("src/main/java/com/example/ui/screens/OnboardingAndOverlays.kt")
        if (!file.exists()) {
            val appFile = File("app/src/main/java/com/example/ui/screens/OnboardingAndOverlays.kt")
            if (appFile.exists()) {
                analyzeBraces(appFile)
            } else {
                println("File not found! Absolute path: " + file.absolutePath)
            }
        } else {
            analyzeBraces(file)
        }
    }

    private fun analyzeBraces(file: File) {
        val code = file.readText()
        val lines = code.split("\n")
        val stack = mutableListOf<Triple<Int, Int, String>>() 
        
        for (i in lines.indices) {
            val line = lines[i]
            if (line.trim().startsWith("//") || line.trim().startsWith("/*") || line.trim().startsWith("*")) continue
            
            var openFun = ""
            val trimmed = line.trim()
            if (trimmed.startsWith("fun ") || trimmed.contains(" fun ") || (trimmed.startsWith("@") && lines.getOrNull(i+1)?.trim()?.startsWith("fun ") == true)) {
                openFun = trimmed
            }
            
            for (j in line.indices) {
                val char = line[j]
                if (char == '{') {
                    stack.add(Triple(i + 1, j + 1, if (openFun.isNotEmpty()) openFun else "line ${i+1}"))
                    openFun = ""
                } else if (char == '}') {
                    if (stack.isEmpty()) {
                        println("ERROR: Unmatched closing brace at Line ${i + 1}, Char ${j + 1}")
                    } else {
                        val last = stack.removeAt(stack.size - 1)
                        if (last.third.contains("fun ") || last.third.startsWith("@")) {
                            println("CLOSED FUN: ${last.third} at Line ${i + 1}")
                        }
                    }
                }
            }
        }
        if (stack.isNotEmpty()) {
            println("ERROR: Unmatched opening braces count: ${stack.size}")
            for (item in stack) {
                if (item.third.contains("fun ") || item.third.startsWith("@")) {
                    println("  Line ${item.first}, Column ${item.second}: ${item.third}")
                }
            }
        } else {
            println("SUCCESS: Braces are balanced!")
        }
    }
}
