package com.mobilicos.smotrofon.util

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Throws(Exception::class)
private fun unpackZip(path: String, zipName: String): Boolean {
    val inputStream = FileInputStream(File(path, zipName).absolutePath)
    var filename: String
    val zis = ZipInputStream(BufferedInputStream(inputStream))
    var ze: ZipEntry
    val buffer = ByteArray(1024)
    var count: Int
    while (zis.nextEntry.also { ze = it } != null) {
        filename = ze.name

        // Need to create directories if not exists, or
        // it will generate an Exception...
        if (ze.isDirectory) {
            val fmd = File(path, filename)
            fmd.mkdirs()
            continue
        }
        val fout = FileOutputStream(File(path, filename).absolutePath)

        // cteni zipu a zapis
        while (zis.read(buffer).also { count = it } != -1) {
            fout.write(buffer, 0, count)
        }
        fout.close()
        zis.closeEntry()
    }
    zis.close()
    File(path, zipName).delete()
    return true
}