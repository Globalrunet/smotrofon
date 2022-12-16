package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.data.models.UpdateVideoDataResponse
import com.mobilicos.smotrofon.data.models.UploadMediaPosterResponse
import com.mobilicos.smotrofon.data.queries.*
import com.mobilicos.smotrofon.data.responses.RemoveMediaResponse
import com.mobilicos.smotrofon.data.responses.UploadVideoResponse
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.network.services.MediaService
import com.mobilicos.smotrofon.network.services.VideoService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import java.io.*
import javax.inject.Inject


class VideoRemoteDataSource @Inject constructor(private val retrofit: Retrofit): RemoteDataSource(retrofit) {

    suspend fun updateVideoData(q: EditVideoQuery): Result<UpdateVideoDataResponse> {
        val videoService = retrofit.create(VideoService::class.java)
        return getResponse(
            request = { videoService.updateVideoData(title = q.title, text = q.text, id = q.id, key = q.key) },
            defaultErrorMessage = "Error fetching Movie list")
    }

    suspend fun removeVideoData(q: RemoveMediaQuery): Result<RemoveMediaResponse> {
        val service = retrofit.create(MediaService::class.java)
        return getResponse(
            request = { service.removeMedia(id = q.id, key = q.key, type = q.type) },
            defaultErrorMessage = "Error removing Media")
    }

    suspend fun uploadVideoPoster(q: UploadMediaPosterQuery): Result<UploadMediaPosterResponse> {
        val service = retrofit.create(MediaService::class.java)

        val file = File(q.image)

        val keyPart: RequestBody = q.key
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val idPart: RequestBody = q.id.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val typePart: RequestBody = q.type.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val filePart = MultipartBody.Part.createFormData(
            "poster",
            file.name,
            file.asRequestBody("image/*".toMediaTypeOrNull())
        )

        return getResponse(
            request = {service.uploadMediaPoster(
                image = filePart,
                id = idPart,
                key = keyPart,
                type = typePart)},
            defaultErrorMessage = "Error fetching Movie list")
    }

    suspend fun uploadVideo(q: UploadVideoQuery): Result<UploadVideoResponse> {
        val service = retrofit.create(MediaService::class.java)

        val file = File(q.path)

        val keyPart: RequestBody = q.key
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val idPart: RequestBody = q.id.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val readFileResult = readChunkOfFileToByteArray(path = q.path, offset = q.next_chunk)
        val fileName = q.path.substringAfterLast("/")
        val filePart = toMultiPartFile(name = "file", filename = fileName, byteArray = readFileResult.byteArray)

        val fileNamePart: RequestBody = fileName
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val nextChunk = q.next_chunk + readFileResult.readedBytes
        val nextChunkPart: RequestBody = nextChunk.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val isEndPart: RequestBody = readFileResult.isEnd.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val sizePart: RequestBody = readFileResult.size.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        val uploadedBytesPart: RequestBody = q.next_chunk.toString()
            .toRequestBody("multipart/form-data".toMediaTypeOrNull())

        return getResponse(
            request = {service.uploadVideo(
                file = filePart,
                id = idPart,
                key = keyPart,
                is_end = isEndPart,
                filename = fileNamePart,
                size = sizePart,
                uploaded_bytes = uploadedBytesPart,
                next_chunk = nextChunkPart)},
            defaultErrorMessage = "Error fetching Movie list")
    }

    fun readChunkOfFileToByteArray(path: String, offset: Long): UploadVideoFileInfo {
        val file = File(path)
        var isEnd = false
        val ous = ByteArrayOutputStream()
        var ios: InputStream? = null
        try {
            val buffer = ByteArray(4096)
            ios = FileInputStream(file)
            ios.skip(offset)
            var read = 0
            var readedChunks = 0

            while (ios.read(buffer).also { read = it } != -1 && readedChunks < 1000) {
                ous.write(buffer, 0, read)
                readedChunks ++
            }
            if (read == -1) isEnd = true
        } finally {
            try {
                ous.close()
            } catch (e: IOException) {}
            try {
                ios?.close()
            } catch (e: IOException) {}
        }
        return UploadVideoFileInfo(byteArray = ous.toByteArray(),
            isEnd = isEnd,
            readedBytes = ous.toByteArray().size,
            size = file.length())
    }

    fun toMultiPartFile(name: String, filename: String, byteArray: ByteArray): MultipartBody.Part {
        val reqFile: RequestBody = byteArray.toRequestBody("video/mp4".toMediaTypeOrNull(), 0, byteArray.size)
        return MultipartBody.Part.createFormData(
            name,
            filename,
            reqFile
        )
    }
}