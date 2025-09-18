package com.example.aiimageapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import com.example.aiimageapp.generator.OfflineGenerator
import kotlin.random.Random

class GenerateViewModel: ViewModel() {
    var prompt: String = ""
    var imageBitmap: Bitmap? = null
    var originalBitmap: Bitmap? = null
    var isGenerating: Boolean = false
    var style: String = "Abstract"

    fun randomize() {
        val seeds = listOf("nebula","sunrise","forest","city","ocean","vaporwave","aurora","retro")
        prompt = seeds[Random.nextInt(seeds.size)]
        generate()
    }

    fun generate() {
        isGenerating = true
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.Default) {
                OfflineGenerator.generateFromPrompt(prompt, style = style)
            }
            imageBitmap = bmp
            originalBitmap = bmp.copy(bmp.config, true)
            isGenerating = false
        }
    }

    fun applyStyle(name: String) {
        imageBitmap?.let { bmp ->
            viewModelScope.launch(Dispatchers.Default) {
                val out = when(name) {
                    "Cartoon" -> OfflineGenerator.styleCartoon(bmp)
                    "Sketch" -> OfflineGenerator.styleSketch(bmp)
                    "Neon" -> OfflineGenerator.styleNeon(bmp)
                    else -> bmp
                }
                imageBitmap = out
            }
        }
    }

    fun adjustBrightnessContrastSaturation(brightness: Float, contrast: Float, saturation: Float) {
        originalBitmap?.let { orig ->
            viewModelScope.launch(Dispatchers.Default) {
                val out = OfflineGenerator.adjustBCS(orig, brightness, contrast, saturation)
                imageBitmap = out
            }
        }
    }
}
