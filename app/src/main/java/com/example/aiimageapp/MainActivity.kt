package com.example.aiimageapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.aiimageapp.ui.GenerateScreen
import com.example.aiimageapp.viewmodel.GenerateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    private val vm = GenerateViewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ask for permission on start (best-effort; user can decline)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val perm = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(perm)
            }
        }

        setContent {
            GenerateScreen(vm,
                onSave = { bmp -> saveBitmapToGallery(bmp) },
                onShare = { bmp -> shareBitmap(bmp) }
            )
        }
    }

    private fun saveBitmapToGallery(bmp: android.graphics.Bitmap) {
        CoroutineScope(Dispatchers.IO).launch {
            val filename = "ai_image_${System.currentTimeMillis()}.png"
            val fos: OutputStream?
            var imageUri: Uri? = null
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/AIImageApp")
                    }
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    imageUri = uri
                    fos = uri?.let { resolver.openOutputStream(it) }
                } else {
                    val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES).toString()
                    val file = java.io.File(imagesDir, filename)
                    fos = java.io.FileOutputStream(file)
                    imageUri = Uri.fromFile(file)
                }
                fos?.use { out -> bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
                runOnUiThread { Toast.makeText(this@MainActivity, "Saved to gallery", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@MainActivity, "Save failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

    private fun shareBitmap(bmp: android.graphics.Bitmap) {
        // save temporarily then share via intent
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val filename = "share_${System.currentTimeMillis()}.png"
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/AIImageApp/Temp")
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { out -> bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, it)
                        type = "image/png"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runOnUiThread { startActivity(Intent.createChooser(shareIntent, "Share image")) }
                } ?: runOnUiThread { Toast.makeText(this@MainActivity, "Share failed", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@MainActivity, "Share failed: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }
}
