package com.mobilicos.smotrofon.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.File
import java.util.zip.ZipFile

class FileUtil {
    companion object {
        var maxWidth = 600
        var maxHeight = 600

        fun getStorageFile(context: Context): File {
            return context.getExternalFilesDir(null) ?: context.filesDir
        }

        private fun checkExternalMedia(): Boolean {
            val stat: Boolean
            val state = Environment.getExternalStorageState()
            stat = when {
                Environment.MEDIA_MOUNTED == state -> {
                    true
                }
                Environment.MEDIA_MOUNTED_READ_ONLY == state -> {
                    false
                }
                else -> {
                    false
                }
            }
            return stat
        }

        @Throws(Exception::class)
        fun unpackZip(path: String, zipName: String): Boolean {

            println("RESULT INSIZE UNZIP")
            ZipFile(File(path, zipName).absolutePath).use { zip ->
                println("RESULT INSIDE ZIP ${File(path, zipName)}")
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { input ->
                        File(path, entry.name).outputStream().use { output ->
                            println(output.toString())
                            input.copyTo(output)
                        }
                    }
                }
            }


//            val inputStream = FileInputStream(File(path, zipName))
//            println("RESULT UNPACK ${File(path, zipName).absolutePath}")
//            var filename: String
//            val zis = ZipInputStream(BufferedInputStream(inputStream))
//            var ze: ZipEntry
//            val buffer = ByteArray(1024)
//            var count: Int
//            while (zis.nextEntry.also { ze = it } != null) {
//                filename = ze.name
//
//                // Need to create directories if not exists, or
//                // it will generate an Exception...
//                if (ze.isDirectory) {
//                    val fmd = File(path, filename)
//                    fmd.mkdirs()
//                    continue
//                }
//                val fout = FileOutputStream(File(path, filename).absolutePath)
//
//                // cteni zipu a zapis
//                while (zis.read(buffer).also { count = it } != -1) {
//                    fout.write(buffer, 0, count)
//                }
//                fout.close()
//                zis.closeEntry()
//            }
//            zis.close()
//            File(path, zipName).delete()
            return true
        }

        fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                while (height / inSampleSize > reqHeight && width / inSampleSize > reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }

        fun getResizedFileBitmap(filePath: String): Bitmap? {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(filePath, options)
        }
    }
}