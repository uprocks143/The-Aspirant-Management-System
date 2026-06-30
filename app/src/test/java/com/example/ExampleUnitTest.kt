package com.example

import org.junit.Test
import java.io.File

class ExampleUnitTest {
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
