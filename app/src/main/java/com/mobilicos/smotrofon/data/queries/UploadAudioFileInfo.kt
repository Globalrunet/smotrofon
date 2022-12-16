package com.mobilicos.smotrofon.data.queries

data class UploadAudioFileInfo(
    val byteArray: ByteArray,
    val isEnd: Boolean,
    val readedBytes: Int,
    val size: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadAudioFileInfo

        if (!byteArray.contentEquals(other.byteArray)) return false
        if (isEnd != other.isEnd) return false
        if (readedBytes != other.readedBytes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = byteArray.contentHashCode()
        result = 31 * result + isEnd.hashCode()
        result = 31 * result + readedBytes
        return result
    }
}