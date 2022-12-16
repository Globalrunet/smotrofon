package com.mobilicos.smotrofon.data

import kotlinx.coroutines.flow.Flow

internal class Repository constructor(dataSource: DataSource = DataSource(DataBase, 2000)) {
    val userData: Flow<String> = dataSource.data
}