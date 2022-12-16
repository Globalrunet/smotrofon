package com.mobilicos.smotrofon.data

import kotlin.random.Random

object DataBase {
    fun fetchData(): Int {
        return Random.nextInt(1000)
    }
}