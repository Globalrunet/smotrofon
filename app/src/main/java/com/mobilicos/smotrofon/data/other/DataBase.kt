package com.mobilicos.smotrofon.data.other

import kotlin.random.Random

object DataBase {
    fun fetchData(): Int {
        return Random.nextInt(1000)
    }
}