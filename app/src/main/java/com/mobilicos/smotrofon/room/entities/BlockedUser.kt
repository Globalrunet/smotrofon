package com.mobilicos.smotrofon.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

@JsonClass(generateAdapter = true)
@Entity(tableName = "blocked_user")
data class BlockedUser (
    @PrimaryKey(autoGenerate = true) val id:Int?,
    @ColumnInfo @Json(name = "user_id") val userId:Int,
    @ColumnInfo @Json(name = "blocked_user_id") val blockedUserId:Int,
    @ColumnInfo @Json(name = "date_added") val dateAdded: Date
)