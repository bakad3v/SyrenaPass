package com.android.syrenapass.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM AppDbModel ORDER BY appName ASC")
    fun getAppsSortedByNameAsc(): Flow<List<AppDbModel>>

    @Query("SELECT * FROM AppDbModel ORDER BY appName DESC")
    fun getAppsSortedByNameDesc(): Flow<List<AppDbModel>>

    @Query("UPDATE AppDbModel SET toDelete=:toDelete WHERE packageName=:packageName")
    suspend fun setDeletionStatus(toDelete: Boolean, packageName: String)

    @Query("UPDATE AppDbModel SET toHide=:toHide WHERE packageName=:packageName")
    suspend fun setHiddenStatus(toHide: Boolean, packageName: String)

    @Query("UPDATE AppDbModel SET toClearData=:toClear WHERE packageName=:packageName")
    suspend fun setClearedStatus(toClear: Boolean, packageName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(app: AppDbModel)

    @Query("DELETE FROM AppDbModel WHERE packageName=:packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM AppDbModel")
    suspend fun clearDb()
}