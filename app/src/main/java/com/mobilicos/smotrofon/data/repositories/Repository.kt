package com.mobilicos.smotrofon.data.repositories

import com.mobilicos.smotrofon.data.other.DataBase
import com.mobilicos.smotrofon.data.remote.DataSource
import kotlinx.coroutines.flow.Flow

internal class Repository constructor(dataSource: DataSource = DataSource(DataBase, 2000)) {
    val userData: Flow<String> = dataSource.data
}