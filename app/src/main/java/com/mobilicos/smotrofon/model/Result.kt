package com.mobilicos.smotrofon.model

/**
 * Generic class for holding success response, error response and loading status
 */
data class Result<out T>(var status: Status, val data: T?, val error: Error?, val message: String?) {

    enum class Status {
        READY,
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T?): Result<T> {
            return Result(Status.SUCCESS, data, null, null)
        }

        fun <T> error(message: String, error: Error?): Result<T> {
            return Result(Status.ERROR, null, error, message)
        }

        fun <T> loading(data: T? = null): Result<T> {
            return Result(Status.LOADING, data, null, null)
        }

        fun <T> ready(): Result<T> {
            return Result(Status.READY, null, null, null)
        }
    }

    override fun toString(): String {
        return "Result(status=$status, data=$data, error=$error, message=$message)"
    }
}