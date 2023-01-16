package com.mobilicos.smotrofon.data

import android.os.Environment
import androidx.lifecycle.viewModelScope
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.Item
import com.mobilicos.smotrofon.data.remote.LessonRemoteDataSource
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.room.dao.LessonDao
import com.mobilicos.smotrofon.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

/**
 * Repository which fetches data from Remote or Local data sources
 */
class LessonRepository @Inject constructor(
        private val lessonRemoteDataSource: LessonRemoteDataSource,
        private val lessonDao: LessonDao
    ) {

    suspend fun downloadLessonByIdent(ident: Int): Flow<Result<ResponseBody>> {
        return flow {
            emit(Result.loading())
            val result = lessonRemoteDataSource.downloadLessonByIdent(ident=ident)
            emit(result)
        }.flowOn(Dispatchers.IO)
    }

    suspend fun downloadLessonByIdentStream(ident: Int, fileToSaveFiles: File): Flow<DownloadState> {
        return lessonRemoteDataSource.downloadLessonByIdentStream(ident=ident)
            .saveFile(ident = ident, fileToSaveFiles = fileToSaveFiles)
    }

    sealed class DownloadState {
        object Ready : DownloadState()
        data class Downloading(val progress: Int) : DownloadState()
        object Finished : DownloadState()
        data class Failed(val error: Throwable? = null) : DownloadState()
    }

    private fun ResponseBody.saveFile(ident: Int, fileToSaveFiles: File): Flow<DownloadState> {
        return flow {
            emit(DownloadState.Downloading(0))

            val outputStream = java.io.FileOutputStream(File(fileToSaveFiles, "$ident.zip"))

            try {
                byteStream().use { inputStream ->
                    outputStream.use { os ->
                        val totalBytes = contentLength()
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var progressBytes = 0L

                        var bytes = inputStream.read(buffer)
                        while (bytes >= 0) {
                            os.write(buffer, 0, bytes)
                            progressBytes += bytes
                            bytes = inputStream.read(buffer)
                            emit(DownloadState.Downloading(((progressBytes * 100) / totalBytes).toInt()))
                        }
                    }
                    outputStream.close()
                }
                FileUtil.unpackZip(fileToSaveFiles.absolutePath, "$ident.zip")
                emit(DownloadState.Finished)
            } catch (e: Exception) {
                emit(DownloadState.Failed(e))
            }
        }.flowOn(Dispatchers.IO).distinctUntilChanged()
    }

    fun insertLesson(lesson: Item) = lessonDao.insert(lesson)

    fun getLessonsIdList() = lessonDao.getLessonsIdList()
}