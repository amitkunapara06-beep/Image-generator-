package com.example.aiimageapp.ui

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import com.example.aiimageapp.viewmodel.GenerateViewModel
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun GenerateScreen(vm: GenerateViewModel, onSave: (android.graphics.Bitmap)->Unit, onShare: (android.graphics.Bitmap)->Unit) {
    val scrollState = rememberScrollState()
    Scaffold(topBar = { TopAppBar(title = { Text("AI Image App (Free Offline)") }) }) { padding ->
        Column(Modifier.padding(16.dp).padding(padding).verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(value = vm.prompt, onValueChange = { vm.prompt = it }, label = { Text("Prompt") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row { Button(onClick = { vm.generate() }, enabled = !vm.isGenerating) { Text("Generate") }; Spacer(Modifier.width(8.dp)); Button(onClick = { vm.randomize() }) { Text("Random") } }
            Spacer(Modifier.height(12.dp))
            Text("Adjustments") 
            Spacer(Modifier.height(6.dp))
            val brightness = remember { mutableStateOf(0f) }
            val contrast = remember { mutableStateOf(1f) }
            val saturation = remember { mutableStateOf(1f) }
            Text("Brightness: ${brightness.value}") 
            Slider(value = brightness.value, onValueChange = { brightness.value = it; vm.adjustBrightnessContrastSaturation(brightness.value, contrast.value, saturation.value) }, valueRange = -1f..1f)
            Text("Contrast: ${"%.2f".format(contrast.value)}") 
            Slider(value = contrast.value, onValueChange = { contrast.value = it; vm.adjustBrightnessContrastSaturation(brightness.value, contrast.value, saturation.value) }, valueRange = 0.5f..2f)
            Text("Saturation: ${"%.2f".format(saturation.value)}") 
            Slider(value = saturation.value, onValueChange = { saturation.value = it; vm.adjustBrightnessContrastSaturation(brightness.value, contrast.value, saturation.value) }, valueRange = 0f..2f)
            Spacer(Modifier.height(12.dp))
            vm.imageBitmap?.let { bmp ->
                Spacer(Modifier.height(8.dp))
                Image(bitmap = bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(480.dp))
                Spacer(Modifier.height(8.dp))
                Row {
                    Button(onClick = { vm.applyStyle("Cartoon") }) { Text("Cartoon") }
                    Spacer(Modifier.width(6.dp))
                    Button(onClick = { vm.applyStyle("Sketch") }) { Text("Sketch") }
                    Spacer(Modifier.width(6.dp))
                    Button(onClick = { vm.applyStyle("Neon") }) { Text("Neon") }
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    IconButton(onClick = { onSave(bmp) }) { Icon(Icons.Default.Save, contentDescription = "Save") }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { onShare(bmp) }) { Icon(Icons.Default.Share, contentDescription = "Share") }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = { vm.randomize() }) { Icon(Icons.Default.Refresh, contentDescription = "Random") }
                }
            }
        }
    }
}
