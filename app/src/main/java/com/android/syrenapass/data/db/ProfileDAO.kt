package com.android.syrenapass.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDAO {
    @Query("SELECT * FROM ProfileDbModel")
    fun getProfiles(): Flow<List<ProfileDbModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(profile: ProfileDbModel)

    @Query("UPDATE ProfileDbModel SET toDelete=:status WHERE id=:id")
    suspend fun setDeletionStatus(id: Int,status: Boolean)

    @Query("DELETE FROM ProfileDbModel WHERE id=:id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM ProfileDbModel")
    suspend fun clearDb()
}