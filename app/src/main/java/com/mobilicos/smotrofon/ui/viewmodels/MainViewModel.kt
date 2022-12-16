package com.mobilicos.smotrofon.ui.viewmodels

import androidx.lifecycle.*
import com.mobilicos.smotrofon.data.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flowOn
import kotlin.random.Random

internal class MainViewModel (
    repository: Repository = Repository()
) : ViewModel() {
    val liveData: LiveData<String> = flow {
        while (true) {
            emit(Random.nextInt(999, 9999).toString())
            kotlinx.coroutines.delay(3000)
        }
    }
        .flowOn(Dispatchers.IO)
        .catch { e ->
            println(e.message)//Error!
        }.asLiveData()
}