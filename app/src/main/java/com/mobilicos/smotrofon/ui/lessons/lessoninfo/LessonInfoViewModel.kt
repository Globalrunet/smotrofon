package com.mobilicos.smotrofon.ui.lessons.lessoninfo

import android.content.ContentValues.TAG
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.LessonRepository
import com.mobilicos.smotrofon.data.LessonRepository.DownloadState
import com.mobilicos.smotrofon.data.models.Item
import com.mobilicos.smotrofon.data.models.LessonItem
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
class LessonInfoViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val moshi: Moshi) : ViewModel() {

    private val _downloadLessonResult = MutableStateFlow<Result<ResponseBody>>(Result.ready())
    val downloadLessonResult: StateFlow<Result<ResponseBody>> = _downloadLessonResult

    private val _downloadLessonStreamResult = MutableStateFlow<DownloadState>(DownloadState.Ready)
    val downloadLessonStreamResult: StateFlow<DownloadState> = _downloadLessonStreamResult

    private lateinit var fileToSaveFiles: File
    private lateinit var currentLanguage: String

    fun getItem(itemIdent: Int): LessonItem? {
        val bufferedReader: BufferedReader = File(
            fileToSaveFiles,
            itemIdent.toString() + "_$currentLanguage.json"
        ).bufferedReader()
        val inputString = bufferedReader.use { it.readText() }

        val jsonAdapter: JsonAdapter<LessonItem> = moshi.adapter(
            LessonItem::class.java
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
            lessonRepository.downloadLessonByIdent(ident = ident).collect {
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

    fun downloadLessonByIdentStream(ident: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_downloadLessonStreamResult.value !is DownloadState.Downloading) {
                lessonRepository.downloadLessonByIdentStream(ident = ident, fileToSaveFiles = fileToSaveFiles).collect { downloadState ->
                    if (downloadState is DownloadState.Finished) {
                        val courseLessonItem = getItem(ident)
                        if (courseLessonItem != null) {
                            insertLessonInfoToDb(lesson = courseLessonItem.item)
                        }
                    }
                    _downloadLessonStreamResult.value = downloadState
                }
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
                outputStream = FileOutputStream(File(fileToSave, "$ident.zip"))
                while (true) {
                    val read: Int = inputStream.read(fileReader)
                    if (read == -1) {
                        break
                    }
                    outputStream.write(fileReader, 0, read)
                    fileSizeDownloaded += read.toLong()
                    Log.d(TAG, "file download: $fileSizeDownloaded of $fileSize")
                }
                outputStream.flush()

                println("RESULT SAVE ZIP ${File(fileToSave, "$ident.zip")}")
                FileUtil.unpackZip(fileToSave.absolutePath, "$ident.zip")

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

    private fun insertLessonInfoToDb(lesson: Item) {
        lessonRepository.insertLesson(lesson = lesson)
    }
}