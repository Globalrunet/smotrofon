package com.mobilicos.smotrofon.ui.courses.lessoninfo

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.data.CoursesLessonRepository
import com.mobilicos.smotrofon.data.models.CourseLesson
import com.mobilicos.smotrofon.data.models.CourseLessonItem
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.util.FileUtil
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.*
import javax.inject.Inject

@HiltViewModel
class CourseLessonInfoViewModel @Inject constructor(
    private val lessonRepository: CoursesLessonRepository,
    private val moshi: Moshi) : ViewModel() {

    private val _downloadLessonResult = MutableStateFlow<Result<ResponseBody>>(Result.ready())
    val downloadLessonResult: StateFlow<Result<ResponseBody>> = _downloadLessonResult
    private lateinit var fileToSaveFiles: File
    private lateinit var currentLanguage: String

    fun getItem(itemIdent: Int): CourseLessonItem? {
        val bufferedReader: BufferedReader = File(
            fileToSaveFiles,
            "c_" + itemIdent.toString() + "_$currentLanguage.json"
        ).bufferedReader()
        val inputString = bufferedReader.use { it.readText() }

        val jsonAdapter: JsonAdapter<CourseLessonItem> = moshi.adapter(
            CourseLessonItem::class.java
        )

        return jsonAdapter.fromJson(inputString)
    }

    fun setFileToSaveFiles(file: File) {
        fileToSaveFiles = file
    }

    fun setCurrentLanguage(language: String) {
        currentLanguage = language
    }

    fun downloadLessonByIdent(ident: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            lessonRepository.downloadCoursesLessonByIdent(ident=ident).collect {
                if (it.status == Result.Status.SUCCESS) {
                    val result = writeResponseBodyToDisk(ident, it.data as ResponseBody)
                    if (result) {
                        val courseLessonItem = getItem(ident)
                        if (courseLessonItem != null) {
                            insertLessonInfoToDb(lesson = courseLessonItem.item)
                        }
                    }
                }
                _downloadLessonResult.value = it
            }
        }
    }

    private fun writeResponseBodyToDisk(ident: Int, body: ResponseBody): Boolean {

        val fileToSave = fileToSaveFiles
        if (!fileToSave.exists()) {
            fileToSave.mkdirs()
        }

        return try {
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null
            try {
                val fileReader = ByteArray(4096)
                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0
                inputStream = body.byteStream()
                outputStream = FileOutputStream(File(fileToSave, "c_$ident.zip"))
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d(TAG, "file saved: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()

                println("RESULT SAVE ZIP ${File(fileToSave, "c_$ident.zip")}")


                val isFileExists = File(fileToSave.absolutePath, "c_$ident.zip").exists()

                if (isFileExists) {
                    println("PREPARE TO UNZIP FILE ${fileToSave.absolutePath} // c_$ident.zip")
                    FileUtil.unpackZip(fileToSave.absolutePath, "c_$ident.zip")
                }

                true
            } catch (e: IOException) {
                false
            } finally {
                inputStream?.close()
                outputStream?.close()
            }
        } catch (e: IOException) {
            false
        }
    }

    private fun insertLessonInfoToDb(lesson: CourseLesson) {
        lessonRepository.insertLesson(lesson = lesson)
    }
}