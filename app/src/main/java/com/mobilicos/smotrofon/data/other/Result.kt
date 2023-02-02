package com.mobilicos.smotrofon.data.other

/**s
 * Generic class for holding success response, error response and loading status
 */
data class ResultOld<out T>(val status: Status, val data: T?, val error: Error?, val message: String?) {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T?): ResultOld<T> {
            return ResultOld(Status.SUCCESS, data, null, null)
        }

        fun <T> error(message: String, error: Error?): ResultOld<T> {
            return ResultOld(Status.ERROR, null, error, message)
        }

        fun <T> loading(data: T? = null): ResultOld<T> {
            return ResultOld(Status.LOADING, data, null, null)
        }
    }

    override fun toString(): String {
        return "Result(status=$status, data=$data, error=$error, message=$message)"
    }
}