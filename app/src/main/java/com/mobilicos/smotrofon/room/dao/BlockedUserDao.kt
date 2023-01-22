package com.mobilicos.smotrofon.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobilicos.smotrofon.room.entities.BlockedUser

@Dao
interface BlockedUserDao {
    @Query("SELECT blockedUserId from blocked_user WHERE userId=:user_id ORDER BY dateAdded ASC")
    fun getBlockedUsersList(user_id: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(blockedUser: BlockedUser)

    @Query("DELETE FROM blocked_user WHERE userId=:user_id AND blockedUserId=:blocked_user_id")
    fun delete(user_id: Int, blocked_user_id: Int)

    @Query("DELETE FROM blocked_user WHERE userId=:user_id")
    fun deleteAll(user_id: Int)
}