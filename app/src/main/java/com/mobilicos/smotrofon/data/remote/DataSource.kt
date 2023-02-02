package com.mobilicos.smotrofon.data.remote

import com.mobilicos.smotrofon.data.other.DataBase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal class DataSource(
    private val dataBase: DataBase = DataBase,
    private val refreshIntervalMs: Long = 1000
) {
    val data: Flow<String> = flow {
        while (true) {
            val dataFromDataBase = DataBase.fetchData()
            emit(dataFromDataBase.toString())
            delay(refreshIntervalMs)
        }
    }
        .flowOn(Dispatchers.Default)
        .catch { e ->
            println(e.message)//Error!
        }
}