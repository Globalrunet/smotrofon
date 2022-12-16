package com.c1ctech.jetpackpagingexp

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() = runBlocking {

        val numbers = 0..10
        val flow: Flow<Int> = numbers.asFlow()

        flow.filter{it % 2 == 0}
            .map {it * 10}
            .collectLatest{
                println(it)
            }

        flow.filter{it % 2 == 1}
            .collect{
                println(it)
            }

        flow.collect{
                println(it)
            }

//        progress(1)
    }

//    @After
//    public fun progress(): Flow<Int> = flow {
//        var progress = 0
//        while (progress < 100) {
//            progress += 2
//            delay(500)
//            println("Progress: $progress")
//            emit(progress)
//        }
//    }
}