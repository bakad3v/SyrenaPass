package com.android.syrenapass.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for usual files settings
 */
@Dao
interface FileDao {

  @Query("SELECT * FROM FileDbModel ORDER BY name ASC")
  fun getDataSortedByPathAsc(): Flow<List<FileDbModel>>

  @Query("SELECT * FROM FileDbModel ORDER BY name DESC")
  fun getDataSortedByPathDesc(): Flow<List<FileDbModel>>

  @Query("SELECT * FROM FileDbModel ORDER BY size ASC")
  fun getDataSortedBySizeAsc(): Flow<List<FileDbModel>>

  @Query("SELECT * FROM FileDbModel ORDER BY size DESC")
  fun getDataSortedBySizeDesc(): Flow<List<FileDbModel>>

  @Query("SELECT * FROM FileDbModel ORDER BY priority ASC")
  fun getDataSortedByPriorityAsc(): Flow<List<FileDbModel>>

  @Query("SELECT * FROM FileDbModel ORDER BY priority DESC")
  fun getDataSortedByPriorityDesc(): Flow<List<FileDbModel>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(file: FileDbModel)

  @Query("UPDATE FileDbModel SET priority=:priority WHERE uri=:uri")
  suspend fun changePriority(priority: Int, uri: String)

  @Query("DELETE FROM FileDbModel WHERE uri=:uri")
  suspend fun delete(uri:String)

  @Query("DELETE FROM FileDbModel")
  suspend fun clearDb()
}
