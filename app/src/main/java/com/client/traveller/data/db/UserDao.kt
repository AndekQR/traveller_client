package com.client.traveller.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.client.traveller.data.db.entities.CURRENT_USER_ID
import com.client.traveller.data.db.entities.User

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User): Long

    @Query("SELECT * FROM user WHERE uid = $CURRENT_USER_ID")
    fun getUser(): LiveData<User>

    @Query("SELECT * FROM user WHERE uid = $CURRENT_USER_ID")
    suspend fun getUserNonLiveData(): User?

    @Query("DELETE FROM user WHERE uid = $CURRENT_USER_ID")
    suspend fun deleteUser()

    @Query("UPDATE user SET verified = 1 WHERE uid = $CURRENT_USER_ID")
    suspend fun setEmailVerified()

}