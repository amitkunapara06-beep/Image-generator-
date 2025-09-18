package com.example.aiimageapp.generator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import java.security.MessageDigest
import kotlin.random.Random
import kotlin.math.pow

object OfflineGenerator {
    private fun seedFromPrompt(prompt: String): Long {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(prompt.toByteArray())
        var seed = 0L
        for (i in 0 until 8) seed = (seed shl 8) or (bytes[i].toLong() and 0xff)
        return seed
    }

    fun generateFromPrompt(prompt: String, width: Int = 1024, height: Int = 1024, style: String = "Abstract"): Bitmap {
        val seed = seedFromPrompt(prompt + style)
        val rnd = Random(seed)
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint()
        paint.isAntiAlias = true

        val c1 = Color.rgb(rnd.nextInt(50,256), rnd.nextInt(50,256), rnd.nextInt(50,256))
        val c2 = Color.rgb(rnd.nextInt(0,210), rnd.nextInt(0,210), rnd.nextInt(0,210))
        for (y in 0 until height) {
            val t = y.toFloat()/height
            val r = (Color.red(c1)*(1-t) + Color.red(c2)*t).toInt()
            val g = (Color.green(c1)*(1-t) + Color.green(c2)*t).toInt()
            val b = (Color.blue(c1)*(1-t) + Color.blue(c2)*t).toInt()
            paint.color = Color.rgb(r,g,b)
            canvas.drawRect(0f, y.toFloat(), width.toFloat(), (y+1).toFloat(), paint)
        }

        val shapes = rnd.nextInt(3,7)
        for (s in 0 until shapes) {
            paint.color = Color.argb(rnd.nextInt(60,200), rnd.nextInt(0,256), rnd.nextInt(0,256), rnd.nextInt(0,256))
            val cx = rnd.nextInt(0, width).toFloat()
            val cy = rnd.nextInt(0, height).toFloat()
            val r = rnd.nextInt(30, width/4).toFloat()
            canvas.drawCircle(cx, cy, r, paint)
        }

        paint.color = Color.WHITE
        paint.textSize = (width / 20).toFloat()
        paint.alpha = 200
        val text = if (prompt.isEmpty()) "Untitled" else prompt.take(60)
        canvas.drawText(text, 20f, (height - 40).toFloat(), paint)

        return bmp
    }

    fun styleCartoon(src: Bitmap): Bitmap { /* simple posterize */ 
        val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        for (y in 0 until bmp.height) for (x in 0 until bmp.width) {
            val c = bmp.getPixel(x,y)
            val r = (android.graphics.Color.red(c)/32)*32
            val g = (android.graphics.Color.green(c)/32)*32
            val b = (android.graphics.Color.blue(c)/32)*32
            bmp.setPixel(x,y, android.graphics.Color.rgb(r,g,b))
        }
        return bmp
    }

    fun styleSketch(src: Bitmap): Bitmap { /* naive edge-like */ 
        val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        for (y in 1 until bmp.height-1) for (x in 1 until bmp.width-1) {
            val c = bmp.getPixel(x,y)
            val c1 = bmp.getPixel(x-1,y)
            val c2 = bmp.getPixel(x+1,y)
            val lum = (android.graphics.Color.red(c)+android.graphics.Color.green(c)+android.graphics.Color.blue(c))/3
            val lum1 = (android.graphics.Color.red(c1)+android.graphics.Color.green(c1)+android.graphics.Color.blue(c1))/3
            val lum2 = (android.graphics.Color.red(c2)+android.graphics.Color.green(c2)+android.graphics.Color.blue(c2))/3
            val edge = ((lum1+lum2)/2 - lum).coerceIn(0,255)
            val col = 255 - edge
            bmp.setPixel(x,y, android.graphics.Color.rgb(col,col,col))
        }
        return bmp
    }

    fun styleNeon(src: Bitmap): Bitmap {
        val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        for (y in 0 until bmp.height step 2) for (x in 0 until bmp.width step 2) {
            val c = bmp.getPixel(x,y)
            val r = 255 - android.graphics.Color.red(c)
            val g = android.graphics.Color.green(c)
            val b = 255 - android.graphics.Color.blue(c)
            bmp.setPixel(x,y, android.graphics.Color.rgb(r,g,b))
        }
        return bmp
    }

    fun adjustBCS(src: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap {
        val bmp = src.copy(Bitmap.Config.ARGB_8888, true)
        val width = bmp.width; val height = bmp.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                val c = bmp.getPixel(x,y)
                var r = android.graphics.Color.red(c).toFloat()
                var g = android.graphics.Color.green(c).toFloat()
                var b = android.graphics.Color.blue(c).toFloat()

                // brightness
                r += 255f * brightness
                g += 255f * brightness
                b += 255f * brightness

                // contrast (simple)
                r = ((r - 128f) * contrast + 128f).coerceIn(0f,255f)
                g = ((g - 128f) * contrast + 128f).coerceIn(0f,255f)
                b = ((b - 128f) * contrast + 128f).coerceIn(0f,255f)

                // saturation (naive)
                val gray = 0.3f*r + 0.59f*g + 0.11f*b
                r = (gray + (r - gray) * saturation).coerceIn(0f,255f)
                g = (gray + (g - gray) * saturation).coerceIn(0f,255f)
                b = (gray + (b - gray) * saturation).coerceIn(0f,255f)

                bmp.setPixel(x,y, android.graphics.Color.rgb(r.toInt(), g.toInt(), b.toInt()))
            }
        }
        return bmp
    }
}
